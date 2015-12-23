package t.n.plainmap;

import gnu.io.CommPortIdentifier;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

public class GpsDeviceComboModel extends DefaultComboBoxModel {
	List<CommPortIdentifier> gpsDeviceCandidates;

	public GpsDeviceComboModel(List<CommPortIdentifier> gpsDeviceCandidates) {
		this.gpsDeviceCandidates = gpsDeviceCandidates;
	}

}
