package ch.manuelroth.gadgetothek_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.bl.Reservation;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;


public class MainViewActivity extends Activity {

    public ListView loanListView;
    public ListView reservationListView;
    public List<Loan> loanList = new ArrayList<Loan>();
    public List<Reservation> reservationList = new ArrayList<Reservation>();
    LoanAdapter loanAdapter = null;
    ReservationAdapter reservationAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        loanListView = (ListView) findViewById(R.id.loanListView);
        LibraryService.getLoansForCustomer(new Callback<List<Loan>>() {
            @Override
            public void notfiy(List<Loan> input) {
                MainViewActivity.this.loanList.addAll(input);
            }
        });

        loanAdapter = new LoanAdapter(this, R.layout.rowlayout, this.loanList);
        loanListView.setAdapter(loanAdapter);

        reservationListView = (ListView) findViewById(R.id.reservationListView);
        LibraryService.getReservationsForCustomer(new Callback<List<Reservation>>() {
            @Override
            public void notfiy(List<Reservation> input) {
                MainViewActivity.this.reservationList.addAll(input);
            }
        });

        reservationAdapter = new ReservationAdapter(this, R.layout.rowlayout, this.reservationList);
        reservationListView.setAdapter(reservationAdapter);

        Button logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LibraryService.logout(new Callback<Boolean>() {
                    @Override
                    public void notfiy(Boolean input) {
                        if(input){
                            Intent intent = new Intent(MainViewActivity.this, LoginActivity.class);
                            startActivity(intent);
                            Context context = MainViewActivity.this.getApplicationContext();
                            CharSequence text = "Logout successful";
                            int duration = Toast.LENGTH_SHORT;
                            Toast.makeText(context, text, duration).show();
                        }else{
                            Context context = MainViewActivity.this.getApplicationContext();
                            CharSequence text = "Logout unsuccessful";
                            int duration = Toast.LENGTH_SHORT;
                            Toast.makeText(context, text, duration).show();
                        }
                    }
                });
            }
        });
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

        public LoanAdapter(Context context, int textViewResourceId, List<Loan> values){
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Loan loan = values.get(position);
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private class ReservationAdapter extends ArrayAdapter<Reservation>{
        private final Context context;
        private final List<Reservation> values;

        public ReservationAdapter(Context context, int textViewResourceId, List<Reservation> values){
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Reservation reservation = values.get(position);

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, null);
            }

            TextView gadgetNameView = (TextView) convertView.findViewById(R.id.gadgetName);
            TextView lentTillView = (TextView) convertView.findViewById(R.id.date);

            gadgetNameView.setText(reservation.getGadget().getName());
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Date reservationDate = reservation.getReservationDate();
            String formattedDate = formatter.format(reservationDate);
            lentTillView.setText(formattedDate);

            return convertView;
        }
    }
}
