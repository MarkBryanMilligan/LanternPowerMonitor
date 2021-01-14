package com.lanternsoftware.zwave.node;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.zwave.controller.Controller;
import com.lanternsoftware.zwave.message.IMessageSubscriber;
import com.lanternsoftware.zwave.message.MessageEngine;
import com.lanternsoftware.zwave.message.impl.ApplicationUpdateRequest;
import com.lanternsoftware.zwave.message.impl.ControllerInitialDataResponse;
import com.lanternsoftware.zwave.message.impl.GetControllerIdResponse;
import com.lanternsoftware.zwave.message.impl.NodeInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NodeManager {
	private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);

	private Controller controller;
	private ControllerInitialDataResponse initialDataResponse;
	private GetControllerIdResponse controllerIdResponse;
	private final Set<Byte> missingNodes = new HashSet<>();
	private Map<Byte, Node> nodes = new HashMap<>();

	public NodeManager(final Controller _controller) {
		controller = _controller;
		MessageEngine.subscribe(new IMessageSubscriber<ControllerInitialDataResponse>() {
			@Override
			public Class<ControllerInitialDataResponse> getHandledMessageClass() {
				return ControllerInitialDataResponse.class;
			}

			@Override
			public void onMessage(ControllerInitialDataResponse _response) {
				synchronized (NodeManager.this) {
					initialDataResponse = _response;
					init();
				}
			}
		});
		MessageEngine.subscribe(new IMessageSubscriber<GetControllerIdResponse>() {
			@Override
			public Class<GetControllerIdResponse> getHandledMessageClass() {
				return GetControllerIdResponse.class;
			}

			@Override
			public void onMessage(GetControllerIdResponse _response) {
				synchronized (NodeManager.this) {
					controllerIdResponse = _response;
					init();
				}
			}
		});
		MessageEngine.subscribe(new IMessageSubscriber<ApplicationUpdateRequest>() {
			@Override
			public Class<ApplicationUpdateRequest> getHandledMessageClass() {
				return ApplicationUpdateRequest.class;
			}

			@Override
			public void onMessage(ApplicationUpdateRequest _request) {
				synchronized (NodeManager.this) {
					if (missingNodes.remove(_request.getNodeId()))
						nodes.put(_request.getNodeId(), new Node(_request.getNodeId(), CollectionUtils.asHashSet(_request.getCommandClasses())));

					logger.debug("Received command classes for node: {}", _request.getNodeId());
					requestNodeInfo();
				}
			}
		});
	}

	private void init() {
		if (!isStarted())
			return;
		missingNodes.clear();
		logger.info("Node Ids:{}", CollectionUtils.transformToCommaSeparated(initialDataResponse.getNodeIds(), String::valueOf));
//		missingNodes.addAll(CollectionUtils.filter(initialDataResponse.getNodeIds(), _b->_b != controllerIdResponse.getControllerId()));
		requestNodeInfo();
	}

	private void requestNodeInfo() {
		if (!missingNodes.isEmpty())
			controller.send(new NodeInfoRequest(CollectionUtils.getFirst(missingNodes)));
		else
			notify();
	}

	private boolean isStarted() {
		return ((initialDataResponse != null) && (controllerIdResponse != null));
	}

	public void waitForStartup() {
		synchronized (this) {
			if (!isStarted()) {
				try {
					wait();
				} catch (InterruptedException _e) {
					_e.printStackTrace();
				}
			}
		}
	}
}
