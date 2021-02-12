package com.lanternsoftware.zwave.controller;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.zwave.message.IMessageSubscriber;
import com.lanternsoftware.zwave.message.Message;
import com.lanternsoftware.zwave.message.MessageEngine;
import com.lanternsoftware.zwave.message.RequestMessage;
import com.lanternsoftware.zwave.message.ResponseMessage;
import com.lanternsoftware.zwave.message.impl.ByteMessage;
import com.lanternsoftware.zwave.message.impl.ControllerCapabilitiesRequest;
import com.lanternsoftware.zwave.message.impl.ControllerInitialDataRequest;
import com.lanternsoftware.zwave.message.impl.GetControllerIdRequest;
import com.lanternsoftware.zwave.message.impl.SendDataRequest;
import com.lanternsoftware.zwave.node.NodeManager;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
	private static final byte SOF = 0x01;
	private static final byte ACK = 0x06;
	private static final byte NAK = 0x15;
	private static final byte CAN = 0x18;

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private SerialPort serialPort;
	private OutputStream os;
	private boolean running = false;
	private AtomicInteger callbackId = new AtomicInteger(0);
	private final Object ackMutex = new Object();
	private final Object responseMutex = new Object();
	private final Object callbackMutex = new Object();
	private boolean responseReceived;
	private final Map<Byte, Byte> callbacks = new HashMap<>();
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	private NodeManager nodeManager;

	public boolean start(String _port) {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(_port);
			serialPort = portIdentifier.open("zwaveport", 2000);
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.disableReceiveThreshold();
			serialPort.enableReceiveTimeout(500);
			os = serialPort.getOutputStream();
			running = true;
			executor.submit(new MessageReceiver());
			MessageEngine.subscribe(new SendDataRequestHandler());
			nodeManager = new NodeManager(this);
			send(new ControllerCapabilitiesRequest());
			send(new ControllerInitialDataRequest());
			send(new GetControllerIdRequest());
			nodeManager.waitForStartup();
			logger.debug("Finishing Controller Start");
			return true;
		} catch (Exception _e) {
			if (serialPort != null) {
				serialPort.close();
				serialPort = null;
			}
			logger.error("Exception while starting controller", _e);
			return false;
		}
	}

	public void stop() {
		running = false;
		ConcurrencyUtils.sleep(2000);
		IOUtils.closeQuietly(os);
		if (serialPort != null) {
			serialPort.close();
		}
		executor.shutdown();
	}

	public void send(Message _message) {
		executor.submit(new MessageSender(_message));
	}

	private class MessageReceiver implements Runnable {
		@Override
		public void run() {
			InputStream is = null;
			try {
				is = serialPort.getInputStream();
				int nextByte = 0;
				int offset = 0;
				while (running) {
					nextByte = is.read();
					if (nextByte == -1)
						continue;
					switch (nextByte) {
						case SOF:
							int messageLength = is.read();
							byte[] buffer = new byte[messageLength + 2];
							buffer[0] = SOF;
							buffer[1] = (byte) messageLength;
							offset = 2;
							while (offset < messageLength + 2) {
								offset += is.read(buffer, offset, messageLength + 2 - offset);
							}
							processIncomingMessage(buffer);
							break;
						case ACK:
							synchronized (ackMutex) {
								logger.debug("Received ACK");
								ackMutex.notify();
							}
							MessageEngine.publish(new ByteMessage((byte) nextByte));
							break;
						case NAK:
						case CAN:
							synchronized (ackMutex) {
								logger.debug("Received: {}", NullUtils.toHex(new byte[]{(byte) nextByte}));
								ackMutex.notify();
							}
							MessageEngine.publish(new ByteMessage((byte) nextByte));
							break;
						default:
							sendRaw(new byte[]{NAK});
							break;
					}
				}
			} catch (IOException _e) {
				logger.error("Exception while receiving inbound, stopping controller", _e);
				stop();
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
	}

	private class MessageSender implements Runnable {
		private final Message message;

		MessageSender(Message _message) {
			message = _message;
		}

		@Override
		public void run() {
			try {
				synchronized (Controller.this) {
					byte callback = 0;
					String log = "Sending message outbound: " + message.describe();
					if (message.isCallbackExpected()) {
						callback = (byte) (callbackId.getAndIncrement() % 126 + 1);
						callbacks.put(callback, message.getNodeId());
						log += " callback: " + callback;
					}
					logger.debug(log);
					byte[] data = message.toByteArray((byte) 0, callback);
					logger.debug("Sending outbound: {}", NullUtils.toHexBytes(data));
					responseReceived = false;
					sendRaw(data);
					synchronized (ackMutex) {
						ackMutex.wait(1000);
					}
					logger.debug("Finished outbound of: {}", message.describe());
				}
				if (message instanceof RequestMessage) {
					logger.debug("Waiting for response from: {}", message.describe());
					synchronized (responseMutex) {
						responseMutex.wait(1000);
						logger.debug("Response received: {}", responseReceived);
						responseReceived = false;
					}
				}
				if (message.isCallbackExpected()) {
					logger.debug("Waiting for callback from: {}", message.describe());
					synchronized (callbackMutex) {
						callbackMutex.wait(1000);
					}
				}
			} catch (InterruptedException _e) {
				logger.error("Interrupted while sending outbound", _e);
			}
		}
	}

	private void sendRaw(byte[] _data) {
		try {
			os.write(_data);
			os.flush();
		} catch (IOException _e) {
			logger.error("IO exception while sending outbound", _e);
		}
	}

	private void processIncomingMessage(byte[] _buffer) {
		logger.debug("Received inbound: {}", NullUtils.toHexBytes(_buffer));
		logger.debug("Sending ACK");
		sendRaw(new byte[]{ACK});
		Message message = MessageEngine.decode(_buffer);
		if (message != null) {
			logger.debug("Received message inbound: {}", message.describe());
			MessageEngine.publish(message);
			if (message instanceof ResponseMessage) {
				synchronized (responseMutex) {
					responseReceived = true;
					responseMutex.notify();
				}
			}
		}
	}

	private class SendDataRequestHandler implements IMessageSubscriber<SendDataRequest> {
		@Override
		public Class<SendDataRequest> getHandledMessageClass() {
			return SendDataRequest.class;
		}

		@Override
		public void onMessage(SendDataRequest _message) {
			Byte nodeId = callbacks.remove(_message.getCallbackId());
			if (nodeId != null) {
				logger.debug("Received callback for node: {} callback id: {}", nodeId, _message.getCallbackId());
				synchronized (callbackMutex) {
					callbackMutex.notify();
				}
			}
		}
	}
}
