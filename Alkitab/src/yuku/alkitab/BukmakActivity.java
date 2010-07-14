
package yuku.alkitab;

import yuku.alkitab.model.*;
import yuku.andoutil.*;
import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;
import android.provider.*;
import android.view.*;
import android.view.ContextMenu.*;
import android.widget.*;
import android.widget.AdapterView.*;

public class BukmakActivity extends ListActivity {
	public static final String EXTRA_ariTerpilih = "ariTerpilih";

	private static final String[] cursorColumnsMapFrom = {AlkitabDb.KOLOM_Bukmak2_ari, AlkitabDb.KOLOM_Bukmak2_tulisan, AlkitabDb.KOLOM_Bukmak2_waktuUbah};
	private static final int[] cursorColumnsMapTo = {R.id.lCuplikan, R.id.lTulisan, R.id.lTanggal};
	private static final String[] cursorColumnsSelect;
	SimpleCursorAdapter adapter;
	SQLiteDatabase db;
	Cursor cursor;
	
	static {
		cursorColumnsSelect = new String[cursorColumnsMapFrom.length+1];
		for (int i = 0; i < cursorColumnsMapFrom.length; i++) {
			cursorColumnsSelect[i+1] = cursorColumnsMapFrom[i];
		}
		cursorColumnsSelect[0] = BaseColumns._ID;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = AlkitabDb.getInstance(getApplicationContext()).getDatabase();
		cursor = db.query(AlkitabDb.TABEL_Bukmak2, cursorColumnsSelect, null, null, null, null, AlkitabDb.KOLOM_Bukmak2_waktuUbah + " desc");
		startManagingCursor(cursor);
		
		adapter = new SimpleCursorAdapter(this, R.layout.bukmak_item, cursor, cursorColumnsMapFrom, cursorColumnsMapTo);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (cursorColumnsSelect[columnIndex] == AlkitabDb.KOLOM_Bukmak2_waktuUbah) {
					String text = Sqlitil.toLocaleDateMedium(cursor.getInt(columnIndex));
					((TextView)view).setText(text);
					return true;
				} else if (cursorColumnsSelect[columnIndex] == AlkitabDb.KOLOM_Bukmak2_ari) {
					int ari = cursor.getInt(columnIndex);
					Kitab kitab = S.xkitab[Ari.toKitab(ari)];
					String[] xayat = S.muatTeks(getResources(), kitab, Ari.toPasal(ari));
					String ayat = xayat[Ari.toAyat(ari) - 1]; // TODO cek out of bounds?
					ayat = U.buangKodeKusus(ayat);
					((TextView)view).setText(ayat);
					return true;
				}
				return false;
			}
		});
		setListAdapter(adapter);
		
		registerForContextMenu(getListView());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor o = (Cursor) adapter.getItem(position);
		int ari = o.getInt(o.getColumnIndexOrThrow(AlkitabDb.KOLOM_Bukmak2_ari));
		
		Intent res = new Intent();
		res.putExtra(EXTRA_ariTerpilih, ari);
		
		setResult(RESULT_OK, res);
		finish();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		new MenuInflater(this).inflate(R.menu.context_bukmak, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuHapusBukmak) {
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
			
			db.delete(AlkitabDb.TABEL_Bukmak2, "_id=?", new String[] {"" + menuInfo.id});
			adapter.getCursor().requery();
			
			return true;
		} else if (item.getItemId() == R.id.menuUbahKeteranganBukmak) {
			// FIXME
			
			return true;
		}
		
		return super.onContextItemSelected(item);
	}
}
