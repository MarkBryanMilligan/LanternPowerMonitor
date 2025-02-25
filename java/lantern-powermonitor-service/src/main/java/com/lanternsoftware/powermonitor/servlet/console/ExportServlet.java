package com.lanternsoftware.powermonitor.servlet.console;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.Breaker;
import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.archive.ArchiveStatus;
import com.lanternsoftware.powermonitor.datamodel.archive.BreakerEnergyArchive;
import com.lanternsoftware.powermonitor.datamodel.archive.DailyEnergyArchive;
import com.lanternsoftware.powermonitor.datamodel.archive.MonthlyEnergyArchive;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import org.apache.commons.io.IOUtils;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@WebServlet("/export/*")
public class ExportServlet extends AuthenticatedConsoleServlet {
	private static final Logger logger = LoggerFactory.getLogger(ExportServlet.class);

	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		TimeZone tz = Globals.dao.getTimeZoneForAccount(_authCode.getAccountId());
		String[] path = path(_req);
		if (path.length > 1) {
			synchronized (this) {
				Date month = new Date(DaoSerializer.toLong(path[0]));
				InputStream is = Globals.dao.streamArchive(_authCode.getAccountId(), month);
				if (is == null) {
					redirect(_rep, _req.getContextPath() + "/export");
					return;
				}
				OutputStream os = null;
				ZipInputStream zis = null;
				ZipOutputStream zos = null;
				JsonWriter jsonWriter = null;
				try {
					os = _rep.getOutputStream();
					if (NullUtils.makeNotNull(path[1]).contains("csv")) {
						BreakerConfig config = Globals.dao.getConfig(_authCode.getAccountId());  //TODO: get historical config for this month in case it's changed since then.
						Map<Integer, Breaker> breakers = CollectionUtils.transformToMap(config.getAllBreakers(), Breaker::getIntKey);
						zos = new ZipOutputStream(os);
						zos.setLevel(Deflater.BEST_SPEED);
						zos.putNextEntry(new ZipEntry(DateUtils.format("MMMM-yyyy", tz, month) + ".csv"));
						zis = new ZipInputStream(is);
						zis.getNextEntry();
						MonthlyEnergyArchive archive = DaoSerializer.fromBson(IOUtils.toByteArray(zis), MonthlyEnergyArchive.class);
						DailyEnergyArchive fday = CollectionUtils.getFirst(archive.getDays());
						if (fday == null) {
							redirect(_rep, _req.getContextPath() + "/export");
							return;
						}
						StringBuilder header = new StringBuilder("\"Timestamp\"");
						for (BreakerEnergyArchive ba : CollectionUtils.makeNotNull(fday.getBreakers())) {
							Breaker b = breakers.get(Breaker.intKey(ba.getPanel(), ba.getSpace()));
							header.append(",\"");
							if (b != null) {
								header.append(b.getKey());
								header.append("-");
								header.append(b.getName());
							}
							header.append("\"");
						}
						header.append("\n");
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
						df.setTimeZone(TimeZone.getTimeZone("UTC"));
						zos.write(NullUtils.toByteArray(header.toString()));
						Date dayStart = archive.getMonth();
						for (DailyEnergyArchive day : CollectionUtils.makeNotNull(archive.getDays())) {
							Date dayEnd = DateUtils.addDays(dayStart, 1, tz);
							int secondsInDay = (int) ((dayEnd.getTime() - dayStart.getTime()) / 1000);
							for (int sec = 0; sec < secondsInDay; sec++) {
								StringBuilder line = new StringBuilder();
								line.append(df.format(new Date(dayStart.getTime() + ((long) sec * 1000))));
								for (BreakerEnergyArchive b : CollectionUtils.makeNotNull(day.getBreakers())) {
									line.append(",");
									if ((b.getReadings() == null) || (sec * 4 >= b.getReadings().length))
										line.append("NaN");
									else {
										ByteBuffer readings = ByteBuffer.wrap(b.getReadings());
										line.append(readings.getFloat(sec * 4));
									}
								}
								line.append("\n");
								zos.write(NullUtils.toByteArray(line.toString()));
							}
							dayStart = dayEnd;
						}
						zos.flush();
						return;
					}
					if (NullUtils.makeNotNull(path[1]).contains("json")) {
						zis = new ZipInputStream(is);
						zis.getNextEntry();
						DaoEntity archive = DaoSerializer.fromBson(IOUtils.toByteArray(zis));
						zos = new ZipOutputStream(os);
						zos.setLevel(Deflater.BEST_SPEED);
						zos.putNextEntry(new ZipEntry(DateUtils.format("MMMM-yyyy", tz, month) + ".json"));
						jsonWriter = new JsonWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8), DaoSerializer.JSON_PRETTY_SETTINGS);
						new DocumentCodec().encode(jsonWriter, archive.toDocument(), EncoderContext.builder().build());
						jsonWriter.flush();
						return;
					}
					IOUtils.copy(is, os);
					return;
				} catch (Exception _e) {
					logger.error("Failed to send archive to browser", _e);
					redirect(_rep, _req.getContextPath() + "/export");
					return;
				} finally {
					IOUtils.closeQuietly(is);
					IOUtils.closeQuietly(jsonWriter);
					IOUtils.closeQuietly(zos);
					IOUtils.closeQuietly(zis);
					IOUtils.closeQuietly(os);
				}
			}
		}
		List<ArchiveStatus> status = Globals.dao.getArchiveStatus(_authCode.getAccountId());
		List<MonthDisplay> months = CollectionUtils.transform(status, _s->new MonthDisplay(DateUtils.format("MMMM yyyy", tz, _s.getMonth()), _s.getMonth().getTime(), (int)_s.getProgress()));
		Map<String, Object> model = model(_req, "months", months);
		model.put("inprogress", CollectionUtils.anyQualify(months, _m->_m.getProgress() > 0 && _m.getProgress() < 100));
		renderBody(_rep, "export.ftl", model);
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		Date month = new Date(DaoSerializer.toLong(_req.getParameter("month")));
		Globals.dao.archiveMonth(_authCode.getAccountId(), month);
		redirect(_rep, ".");
	}
}
