package ch.manuelroth.gadgetothek_android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Gadget;
import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.bl.Reservation;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;


public class MainViewActivity extends Activity {

    public ListView loanListView;
    public ListView reservationListView;
    public List<Loan> loanList;
    public List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        loanListView = (ListView) findViewById(R.id.loanListView);
        LibraryService.getLoansForCustomer(new Callback<List<Loan>>() {
            @Override
            public void notfiy(List<Loan> input) {
                MainViewActivity.this.loanList = input;
            }
        });

        if(loanList == null){
            loanList = new ArrayList<Loan>();
        }

        LoanAdapter loanAdapter = new LoanAdapter(this, loanList);
        loanListView.setAdapter(loanAdapter);

        reservationListView = (ListView) findViewById(R.id.reservationListView);
        LibraryService.getReservationsForCustomer(new Callback<List<Reservation>>() {
            @Override
            public void notfiy(List<Reservation> input) {
                MainViewActivity.this.reservationList = input;
            }
        });
        if(reservationList == null){
            reservationList = new ArrayList<Reservation>();
        }

        ReservationAdapter reservationAdapter = new ReservationAdapter(this, reservationList);
        reservationListView.setAdapter(reservationAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoanAdapter extends ArrayAdapter<Loan>{
        private final Context context;
        private final List<Loan> values;

        public LoanAdapter(Context context, List<Loan> values){
            super(context, R.layout.rowlayout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Loan loan = values.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, parent, false);
            }

            TextView gadgetNameView = (TextView) convertView.findViewById(R.id.gadgetName);
            TextView dueDateView = (TextView) convertView.findViewById(R.id.date);

            gadgetNameView.setText(loan.getGadget().getName());
            dueDateView.setText(loan.overDueDate().toString());

            return convertView;
        }
    }

    private class ReservationAdapter extends ArrayAdapter<Reservation>{
        private final Context context;
        private final List<Reservation> values;

        public ReservationAdapter(Context context, List<Reservation> values){
            super(context, R.layout.rowlayout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Reservation reservation = values.get(position);

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
            }

            TextView gadgetNameView = (TextView) convertView.findViewById(R.id.gadgetName);
            TextView lentTillView = (TextView) convertView.findViewById(R.id.date);

            gadgetNameView.setText(reservation.getGadget().getName());
            lentTillView.setText(reservation.getReservationDate().toString());

            return convertView;
        }
    }
}
