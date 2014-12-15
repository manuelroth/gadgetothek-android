package ch.manuelroth.gadgetothek_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Gadget;
import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.bl.Reservation;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;

import static android.R.layout.simple_list_item_multiple_choice;


public class ReservationActivity extends Activity {

    private EditText searchTextView;
    private ListView gadgetListView;
    private List<Gadget> gadgetList = new ArrayList<Gadget>();
    private ArrayAdapter gadgetAdapter = null;
    private List<Reservation> reservationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        gadgetListView = (ListView) ReservationActivity.this.findViewById(R.id.gadgetListView);
        gadgetListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        gadgetListView.setItemsCanFocus(false);

        gadgetAdapter = new ArrayAdapter<Gadget>(this, simple_list_item_multiple_choice, new ArrayList<Gadget>());
        gadgetListView.setAdapter(gadgetAdapter);
        LibraryService.getReservationsForCustomer(input -> {
            reservationList.addAll(input);
        });
        LibraryService.getGadgets(input -> {
            for(Reservation reservation : reservationList){
                if(input.contains(reservation.getGadget())){
                    input.remove(reservation.getGadget());
                }
            }
            gadgetAdapter.addAll(input);
        });


        LibraryService.getGadgets(input -> gadgetList.addAll(input));
        gadgetAdapter.addAll(gadgetList);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(gadgetListView);
        fab.setOnClickListener(v -> {
            SparseBooleanArray sp = gadgetListView.getCheckedItemPositions();
            int numberOfCheckedItems = getNumberOfCheckedItems(sp);
            int numberOfReservations = numberOfCheckedItems + getIntent().getIntExtra("numberOfReservations", 0);
            if (numberOfCheckedItems == 0) {
                Context context = ReservationActivity.this.getApplicationContext();
                CharSequence text = "Kein Gadget ist ausgewählt";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            } else if (numberOfReservations > 3) {
                Context context = ReservationActivity.this.getApplicationContext();
                CharSequence text = "Es sind immer nur drei Reservationen zur gleichen Zeit erlaubt";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
            } else {
                for (int i = 0; i < sp.size(); i++) {
                    int gadgetIndex = sp.keyAt(i);
                    if (sp.valueAt(i)) {
                        Gadget gadget = (Gadget) gadgetAdapter.getItem(gadgetIndex);

                        LibraryService.reserveGadget(gadget, new Callback<List<Loan>>() {
                            @Override
                            public void notfiy(List<Loan> input) {
                            }
                        });
                    }
                }
                Context context = ReservationActivity.this.getApplicationContext();
                Intent intent = new Intent(context, MainViewActivity.class);
                intent.putExtra("Switch tab", 2);
                startActivity(intent);
            }
        });
        searchTextView = (EditText) ReservationActivity.this.findViewById(R.id.editText);
        searchTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = searchTextView.getText().toString().toLowerCase();
                filter(text);
            }
        });
    }

    private void filter(String filterText) {
        filterText = filterText.toLowerCase();
        gadgetAdapter.clear();
        if (filterText.length() == 0) {
            gadgetAdapter.addAll(gadgetList);
        }
        else
        {


            for (Gadget gadget : gadgetList)
            {
                if (gadget.getName().toLowerCase().contains(filterText))
                {
                    gadgetAdapter.add(gadget);
                }
            }
        }
        gadgetListView.setAdapter(gadgetAdapter);
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

    private int getNumberOfCheckedItems(SparseBooleanArray sp) {
        int numberOfCheckedItems = 0;

        for (int i = 0; i < sp.size(); i++) {
            if (sp.valueAt(i)) {
                ++numberOfCheckedItems;
            }
        }
        return numberOfCheckedItems;
    }
}
