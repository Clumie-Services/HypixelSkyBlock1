package net.swofty.service.orchestrator.endpoints;

import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.objects.orchestrator.GetServerForMapProtocolObject;
import net.swofty.service.generic.redis.ServiceEndpoint;

public class GetServerForMapEndpoint implements ServiceEndpoint
		<GetServerForMapProtocolObject.GetServerForMapMessage,
				GetServerForMapProtocolObject.GetServerForMapResponse> {

	@Override
	public ProtocolObject<GetServerForMapProtocolObject.GetServerForMapMessage, GetServerForMapProtocolObject.GetServerForMapResponse> associatedProtocolObject() {
		return new GetServerForMapProtocolObject();
	}

	@Override
	public GetServerForMapProtocolObject.GetServerForMapResponse onMessage(ServiceProxyRequest message,
																		   GetServerForMapProtocolObject.GetServerForMapMessage body) {
		return new GetServerForMapProtocolObject.GetServerForMapResponse(null, null);
	}
}
