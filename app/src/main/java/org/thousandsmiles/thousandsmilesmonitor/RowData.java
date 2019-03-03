package org.thousandsmiles.thousandsmilesmonitor;

/*
 * (C) Copyright Syd Logan 2018-2019
 * (C) Copyright Thousand Smiles Foundation 2018-2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class RowData implements Parcelable {
    private String m_rowdata;
    private boolean m_isWaitingItem;
    private int m_clinicstation;
    private int m_queue;
    private int m_patientid;
    private int m_routingslipentry;
    private int m_routingslip;
    private String m_clinicStationName;
    private boolean m_isMale;
    private String m_patientName;
    private boolean m_isNewPatient;
    private boolean m_isXray;
    private boolean m_isCurrentXray;

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.m_rowdata);
        dest.writeInt(this.m_clinicstation);
        dest.writeInt(this.m_queue);
        dest.writeInt(m_patientid);
        dest.writeInt(m_routingslipentry);
        dest.writeInt(m_routingslip);
        dest.writeString(m_clinicStationName);
        dest.writeInt(this.m_isMale == true ? 1 : 0);
        dest.writeString(this.m_patientName);
        dest.writeInt(this.m_isNewPatient == true ? 1 : 0);
        dest.writeInt(this.m_isXray == true ? 1 : 0);
        dest.writeInt(this.m_isCurrentXray == true ? 1 : 0);
    }

    public static final Parcelable.Creator<RowData> CREATOR
            = new Parcelable.Creator<RowData>() {
        public RowData createFromParcel(Parcel in) {
            return new RowData(in);
        }

        public RowData[] newArray(int size) {
            return new RowData[size];
        }
    };

    public RowData() {
        m_routingslip = 0;
        m_routingslipentry = 0;
        m_patientid = 0;
        m_queue = 0;
        m_clinicstation = 0;
        m_rowdata = "";
        m_clinicStationName = "";
        m_isMale = false;
        m_patientName = "";
        m_isNewPatient = false;
        m_isXray = false;
        m_isCurrentXray = false;
    }

    private RowData(Parcel in) {
        m_isCurrentXray = in.readInt() == 1;
        m_isXray = in.readInt() == 1;
        m_isNewPatient = in.readInt() == 1;
        m_patientName = in.readString();
        m_isMale = in.readInt() == 1;
        m_clinicStationName = in.readString();
        m_routingslip = in.readInt();
        m_routingslipentry = in.readInt();
        m_patientid = in.readInt();
        m_queue = in.readInt();
        m_clinicstation = in.readInt();
        m_rowdata = in.readString();
    }

    public void setClinicstation(int clinicstation) {
        m_clinicstation = clinicstation;
    }

    public int getClinicstation() {
        return m_clinicstation;
    }

    public void setClinicStationName(String name)
    {
        m_clinicStationName = name;
    }

    public String getClinicStationName()
    {
        return m_clinicStationName;
    }

    public void setPatientName(String name)
    {
        m_patientName = name;
    }

    public String getPatientName()
    {
        return m_patientName;
    }

    public int getQueue() {
        return m_queue;
    }

    public void setQueue(int queue) {
        m_queue = queue;
    }

    public int getPatientid() {
        return m_patientid;
    }

    public void setPatientid(int patientid) {
        m_patientid = patientid;
    }

    public int getRoutingSlipEntry() {
        return m_routingslipentry;
    }

    public void setRoutingSlipEntry(int routingslipentry) {
        m_routingslipentry = routingslipentry;
    }

    public int getRoutingSlip() {
        return m_routingslip;
    }

    public void setRoutingSlip(int routingslip) {
        m_routingslip = routingslip;
    }

    public String getRowdata() {
        return m_rowdata;
    }

    public void setRowdata(String rowdata) {
        m_rowdata = rowdata;
    }

    public boolean isWaitingItem() {
        return m_isWaitingItem;
    }

    public void setWaitingItem(boolean waitingItem) {
        m_isWaitingItem = waitingItem;
    }

    public boolean isMale() {
        return m_isMale;
    }

    public void setIsMale(boolean isMale) {
        m_isMale = isMale;
    }

    public boolean isNewPatient() {
        return m_isNewPatient;
    }

    public void setIsNewPatient(boolean isNewPatient) {
        m_isNewPatient = isNewPatient;
    }

    public boolean isXray() {
        return m_isXray;
    }

    public void setIsXray(boolean isXray) {
        m_isXray = isXray;
    }

    public boolean isCurrentXray() {
        return m_isCurrentXray;
    }

    public void setIsCurrentXray(boolean isCurrentXray) {
        m_isCurrentXray = isCurrentXray;
    }
}
