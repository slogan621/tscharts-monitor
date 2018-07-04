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
import android.widget.TextView;

public class DeleteFromQueueDialogFragment extends DialogFragment {

    private View m_view;
    RowData m_rd;
    Activity m_activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m_rd = getArguments().getParcelable(null);
        m_activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.delete_queue_dialog, null);
        TextView text = (TextView) m_view.findViewById(R.id.title);

        String title = String.format(getString(R.string.msg_remove_from_queue), m_rd.getPatientid());
        text.setText(title);

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AsyncTask task = new DeleteFromQueueTask();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) m_rd, (Object) m_activity);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_delete_queue_dialog);
        return ret;
    }
}