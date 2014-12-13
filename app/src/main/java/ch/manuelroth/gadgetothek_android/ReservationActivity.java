package ch.manuelroth.gadgetothek_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ch.manuelroth.gadgetothek_android.bl.Gadget;
import ch.manuelroth.gadgetothek_android.bl.Loan;
import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;


public class ReservationActivity extends Activity {

    private ListView gadgetListView;
    private ArrayAdapter gadgetAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        gadgetListView = (ListView) ReservationActivity.this.findViewById(R.id.gadgetListView);
        gadgetListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        gadgetListView.setItemsCanFocus(false);

        gadgetAdapter = new ArrayAdapter<Gadget>(this, android.R.layout.simple_list_item_multiple_choice, new ArrayList<>());
        gadgetListView.setAdapter(gadgetAdapter);

        LibraryService.getGadgets(input -> gadgetAdapter.addAll(input));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(gadgetListView);
        fab.setOnClickListener(v -> {
            SparseBooleanArray sp = gadgetListView.getCheckedItemPositions();
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
            startActivity(intent);
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
}
