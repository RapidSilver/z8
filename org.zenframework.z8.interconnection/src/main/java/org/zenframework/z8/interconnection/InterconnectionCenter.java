package org.zenframework.z8.interconnection;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.HubServer;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.IInterconnectionCenter;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.RequestDispatcher;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ArrayUtils;

public class InterconnectionCenter extends HubServer implements IInterconnectionCenter {

	private static final String cache = "interconnection.center.cache";

	static public String id = guid.create().toString();

	static private InterconnectionCenter instance = null;

	public static IInterconnectionCenter launch(ServerConfig config) throws RemoteException {
		if(instance == null) {
			instance = new InterconnectionCenter();
			instance.start();
		}
		return instance;
	}

	private InterconnectionCenter() throws RemoteException {
		super(ServerConfig.interconnectionCenterPort());
	}

	@Override
	public String id() throws RemoteException {
		return id;
	}

	@Override
	public void start() throws RemoteException {
		super.start();

		Trace.logEvent("JVM startup options: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString() + "\n\t" + RequestDispatcher.getMemoryUsage());
	}

	@Override
	public void register(IApplicationServer server) throws RemoteException {
		String[] domains = server.domains();
		addServer(new ServerInfo(server, domains));
	}

	@Override
	public void unregister(IApplicationServer server) throws RemoteException {
		removeServer(server);
	}

	@Override
	public IApplicationServer connect(String domain) throws RemoteException {
		IServerInfo server = findServer(domain);
		return server != null ? server.getServer() : null;
	}

	@Override
	public boolean has(IApplicationServer server, Message message) throws RemoteException {
		return server.has(message);
	}

	@Override
	public boolean accept(IApplicationServer server, Message message) throws RemoteException {
		return server.accept(message);
	}

	@Override
	protected File cacheFile() {
		return new File(Folders.Base, cache);
	}

	private IServerInfo findServer(String domain) throws RemoteException {
		IServerInfo[] servers = getServers();

		for(IServerInfo server : servers) {
			if(!ArrayUtils.contains(server.getDomains(), domain))
				continue;

			if(!server.isAlive()) {
				if(server.isDead())
					unregister(server.getServer());
				continue;
			}

			// меняем порядок, чтобы распределять запросы
			sendToBottom(server);

			return server;
		}

		return null;
	}
}
