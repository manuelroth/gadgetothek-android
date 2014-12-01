package ch.manuelroth.gadgetothek_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;

public class AusleihenFragment extends Fragment {

    private ListView loanListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ausleihen, container, false);
        loanListView = (ListView) rootView.findViewById(R.id.loanListView);
        LoanAdapter loanAdapter = new LoanAdapter(this.getActivity(), R.layout.rowlayout, new ArrayList<>());
        loanListView.setAdapter(loanAdapter);

        LibraryService.getLoansForCustomer(input -> AusleihenFragment.this.getActivity().runOnUiThread(() -> {
            loanAdapter.clear();
            loanAdapter.addAll(input);
        }));

        return rootView;
    }

    private class LoanAdapter extends ArrayAdapter<Loan> {
        private final Context context;
        private final List<Loan> values;

        public LoanAdapter(Context context, int textViewResourceId, List<Loan> values){
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Loan loan = values.get(position);
            if(convertView == null){

                LayoutInflater inflater = (LayoutInflater) AusleihenFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, null);
            }

            TextView gadgetNameView = (TextView) convertView.findViewById(R.id.gadgetName);
            TextView dueDateView = (TextView) convertView.findViewById(R.id.date);

            gadgetNameView.setText(loan.getGadget().getName());
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Date overDueDate = loan.overDueDate();
            String formattedDate = formatter.format(overDueDate);
            dueDateView.setText(formattedDate);

            return convertView;
        }
    }
}
