package edu.sc.snacktrack.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.utils.SnackExporter;

public class BrowseExportsDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_exports_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        TextView instructions = (TextView) view.findViewById(R.id.instructionsTextView);
        instructions.append(SnackExporter.getExportDirectory().getAbsolutePath());
        return view;
    }
}
