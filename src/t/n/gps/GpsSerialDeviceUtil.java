package t.n.gps;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import t.n.map.OsType;

public class GpsSerialDeviceUtil {

	public GpsSerialDeviceUtil() {
	}

	public static List<CommPortIdentifier> getCandidateSerialPorts(OsType osType) {
		List<CommPortIdentifier> candidateDevices = new ArrayList<>();
		Enumeration<CommPortIdentifier> portCandidates = CommPortIdentifier.getPortIdentifiers();
		while(portCandidates.hasMoreElements()) {
			CommPortIdentifier port = portCandidates.nextElement();
			String portName = port.getName();
			switch(osType) {
			case win:
				candidateDevices.add(port);
				break;
			case osx:
				if(portName.startsWith("/dev/tty.usbserial")) {
					candidateDevices.add(port);
				}
				break;
			case unix:
				candidateDevices.add(port);
				break;
			}
		}

		return candidateDevices;
	}
}
