package it.univaq.mwt.ifame.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import it.univaq.mwt.ifame.R;

public class LoadingSpinnerDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);

        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_loading_spinner);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }
}
