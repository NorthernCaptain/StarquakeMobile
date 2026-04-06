package northern.captain.quadronia.android.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import northern.captain.quadronia.android.R;
import northern.captain.gamecore.glx.INameDialog;
import northern.captain.gamecore.glx.NContext;
import northern.captain.tools.StringUtils;

/**
 * Created by leo on 24.05.15.
 */
public class NameDialog implements INameDialog, Runnable
{
    AlertDialog dialog;
    EditText nameEdit;

    INameDialog.OnOkCallback callback;
    String nameEntered;

    public void create(String oldName)
    {
        LayoutInflater inflater = LayoutInflater.from(AndroidContext.activity);
        View view = inflater.inflate(R.layout.namedlg, null);

        nameEdit = (EditText)view.findViewById(R.id.nameEditFld);
        if(oldName != null) nameEdit.setText(oldName);

        AlertDialog.Builder builder = new AlertDialog.Builder(AndroidContext.activity);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialog.cancel());

        dialog = builder.create();
    }

    private void doOK()
    {
        nameEntered = nameEdit.getText().toString().trim();

        if(StringUtils.isNullOrEmpty(nameEntered))
        {
            Toast toast = Toast.makeText(AndroidContext.activity, R.string.wrongname, Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        NContext.current.post(this);
        dialog.dismiss();
    }

    @Override
    public void show(OnOkCallback callback)
    {
        this.callback = callback;
        AndroidContext.mainHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                create(null);
                dialog.show();
                dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        doOK();
                    }
                });
            }
        });
    }

    @Override
    public void run()
    {
        if(callback != null)
        {
            callback.onOk(nameEntered);
        }
    }
}
