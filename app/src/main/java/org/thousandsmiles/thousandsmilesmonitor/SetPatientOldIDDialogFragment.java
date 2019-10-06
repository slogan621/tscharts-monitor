/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
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

package org.thousandsmiles.thousandsmilesmonitor;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.thousandsmiles.tscharts_lib.HideyHelper;
import org.thousandsmiles.tscharts_lib.PatientData;

public class SetPatientOldIDDialogFragment extends DialogFragment {

    private View m_view;
    RowData m_rd;
    Activity m_activity;
    PatientData m_patientData = new PatientData();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m_rd = getArguments().getParcelable(null);
        m_activity = getActivity();
        m_patientData.fromJSONObject(SessionSingleton.getInstance().getPatientData(m_rd.getPatientid()));

        AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.edit_patient_oldid_dialog, null);
        EditText text = (EditText) m_view.findViewById(R.id.value_oldid);
        ;
        text.setText(String.format("%d", m_patientData.getOldId()));

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.msg_button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AsyncTask task = new MarkPatientRemovedTask();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) m_rd, (Object) m_activity);
                        dialog.dismiss();
                        HideyHelper h = new HideyHelper();
                        h.toggleHideyBar(m_activity);
                    }
                })
                .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        HideyHelper h = new HideyHelper();
                        h.toggleHideyBar(m_activity);
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_set_patient_old_id_dialog);
        return ret;
    }
}