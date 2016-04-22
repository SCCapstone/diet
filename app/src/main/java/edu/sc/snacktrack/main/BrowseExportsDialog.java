package edu.sc.snacktrack.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.parse.ParseUser;

import edu.sc.snacktrack.R;
import edu.sc.snacktrack.snacks.SnackList;
import edu.sc.snacktrack.utils.SnackExporter;

public class BrowseExportsDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_exports_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        TextView myInstructions = (TextView) view.findViewById(R.id.browseMyExportsInstructions);
        TextView otherInstructions = (TextView) view.findViewById(R.id.browseOtherExportsInstructions);
        myInstructions.append(SnackExporter.getExportDirectory(SnackList.getInstance().getUser()).getAbsolutePath());
        otherInstructions.append(SnackExporter.getExportDirectory(SnackList.getInstance().getUser()).getAbsolutePath());

        if(ParseUser.getCurrentUser().equals(SnackList.getInstance().getUser())){
            myInstructions.setVisibility(View.VISIBLE);
            otherInstructions.setVisibility(View.GONE);
        } else{
            myInstructions.setVisibility(View.GONE);
            otherInstructions.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
