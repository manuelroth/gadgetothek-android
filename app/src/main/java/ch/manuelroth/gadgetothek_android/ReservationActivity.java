package ch.manuelroth.gadgetothek_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Gadget;
import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;


public class ReservationActivity extends Activity {

    private ListView gadgetListView;
    private List<Gadget> gadgetList = new ArrayList<Gadget>();
    private GadgetAdapter gadgetAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        gadgetListView = (ListView) ReservationActivity.this.findViewById(R.id.reservationListView);

        LibraryService.getGadgets(new Callback<List<Gadget>>() {
            @Override
            public void notfiy(List<Gadget> input) {
                gadgetList.addAll(input);
            }
        });

        gadgetAdapter = new GadgetAdapter(this, R.layout.rowlayout, this.gadgetList);
        gadgetListView.setAdapter(gadgetAdapter);
        gadgetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Gadget gadget = (Gadget) parent.getItemAtPosition(position);
                AlertDialog.Builder adb=new AlertDialog.Builder(ReservationActivity.this);
                adb.setTitle("Submit reservation");
                adb.setMessage("Are you sure you want to reserve this gadget?" + position);
                final int positionToReserve = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //I have no idea why this method would return a List<Loan> but have stupidly implemented it that way regardless - doesn't seem to work though...
                        LibraryService.reserveGadget(gadget, new Callback<List<Loan>>() {
                            @Override
                            public void notfiy(List<Loan> input) {
                                if(input.size() > 0){
                                    Context context = ReservationActivity.this.getApplicationContext();
                                    CharSequence text = "Reservation successfully submitted.";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast.makeText(context, text, duration).show();
                                }else{
                                    Context context = ReservationActivity.this.getApplicationContext();
                                    CharSequence text = "Reservation could not be submitted!";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast.makeText(context, text, duration).show();
                                }
                            }
                        });
                    }});
                adb.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reservation, menu);
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

    private class GadgetAdapter extends ArrayAdapter<Gadget> {
        private final Context context;
        private final List<Gadget> values;

        public GadgetAdapter(Context context, int textViewResourceId, List<Gadget> values){
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Gadget gadget = values.get(position);

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.rowlayout, null);
            }

            TextView gadgetNameView = (TextView) convertView.findViewById(R.id.gadgetName);

            gadgetNameView.setText(gadget.getName());

            return convertView;
        }
    }
}
