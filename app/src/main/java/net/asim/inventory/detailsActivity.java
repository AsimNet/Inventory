package net.asim.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.asim.inventory.Data.InventoryContract;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static net.asim.inventory.R.id.fab;

public class detailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final int PICK_PHOTO_REQUEST = 20;
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;

    //Validation Variables
    private boolean mProductHasChanged = false;

    @BindView(R.id.product_name)
    protected EditText produuctName;

    @BindView(R.id.quantity)
    protected EditText productQuantity;

    @BindView(R.id.price)
    protected EditText productPrice;

    @BindView(R.id.sold)
    protected EditText productSold;

    @BindView(R.id.suppliers_number)
    protected EditText suppliersNumber;

    @BindView(R.id.image)
    ImageView productImage;

    @BindView(R.id.fab)
    protected FloatingActionButton makeCallBTN;

    private Uri mCurrentProductUri; //current product _ID in edit mode null in insert mode

    private String mCurrentPhotoUri = "no images";


    private static final int EXISTING_INVENTORY_LOADER = 0;

    //General Product QUERY PROJECTION
    public final String[] PRODUCT_COLS = {
            InventoryContract.ProductEntry._ID,
            InventoryContract.ProductEntry.COL_NAME,
            InventoryContract.ProductEntry.COL_QUANTITY,
            InventoryContract.ProductEntry.COL_PRICE,
            InventoryContract.ProductEntry.COL_ITEMS_SOLD,
            InventoryContract.ProductEntry.COL_SUPPLIER,
            InventoryContract.ProductEntry.COL_PICTURE
    };

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);


        //set on touch listeners
        produuctName.setOnTouchListener(mTouchListener);
        productQuantity.setOnTouchListener(mTouchListener);
        productPrice.setOnTouchListener(mTouchListener);
        productSold.setOnTouchListener(mTouchListener);
        suppliersNumber.setOnTouchListener(mTouchListener);
        productImage.setOnTouchListener(mTouchListener);


        //Check where we came from
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            //User click new product
            setTitle(getString(R.string.add_product));

            makeCallBTN.setVisibility(View.GONE);
        } else {

            makeCallBTN.setVisibility(View.VISIBLE);

            //Read database for selected Product
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuOpen = menu.findItem(R.id.delete);
        if (mCurrentProductUri == null) {
            menuOpen.setVisible(false);
        } else {
            menuOpen.setVisible(true);

        }
        return true;
    }

    @OnClick(fab)
    protected void callBTN() {
        //
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + suppliersNumber.getText().toString().trim()));
        try {
            startActivity(in);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Could not find an activity to place the call.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.image)
    protected void changeImage(View view) {
        //change product photo
        onPhotoProductUpdate(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                // User chose the "Delete" item..
                showDeleteConfirmationDialog();
                return true;

            case R.id.save:
                //user chose the "Save" button
                saveProduct();
                return true;

            case android.R.id.home:
                //if user didnt make changes
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(detailsActivity.this);
                    return true;
                }

                //User has made som change

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(detailsActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void onPhotoProductUpdate(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //We are on M or above so we need to ask for runtime permissions
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallaray();
            } else {
                // we are here if we do not all ready have permissions
                String[] permisionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE};
                requestPermissions(permisionRequest, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            //We are on an older devices so we dont have to ask for runtime permissions
            getImageFromGallaray();
        }

    }


    private void getImageFromGallaray() {
        // invoke the image gallery using an implict intent.
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {
        //Go back if we have no changes
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        //otherwise Protect user from loosing info
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Get user input from editor and save/update product into database.
     */
    private void saveProduct() {
        //Read Values from text field
        String nameString = produuctName.getText().toString().trim();
        String QuantityString = productQuantity.getText().toString().trim();
        String salesString = productSold.getText().toString().trim();
        String priceString = productPrice.getText().toString().trim();
        String supplierString = suppliersNumber.getText().toString().trim();

        //Check if is new or if an update
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(QuantityString) || TextUtils.isEmpty(salesString)
                || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierString)) {

            Toast.makeText(this, R.string.err_fill_all_fields, Toast.LENGTH_SHORT).show();
            // No change has been made so we can return
            return;
        }

        //We set values for insert update
        ContentValues values = new ContentValues();

        values.put(InventoryContract.ProductEntry.COL_NAME, nameString);
        values.put(InventoryContract.ProductEntry.COL_QUANTITY, QuantityString);
        values.put(InventoryContract.ProductEntry.COL_ITEMS_SOLD, salesString);
        values.put(InventoryContract.ProductEntry.COL_PRICE, priceString);
        values.put(InventoryContract.ProductEntry.COL_SUPPLIER, supplierString);
        values.put(InventoryContract.ProductEntry.COL_PICTURE, mCurrentPhotoUri);

        if (mCurrentProductUri == null) {

            Uri insertedRow = getContentResolver().insert(InventoryContract.ProductEntry.CONTENT_URI, values);

            if (insertedRow == null) {
                Toast.makeText(this, R.string.try_again, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.updated_success, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            // We are Updating
            int rowUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.try_again, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.updated_success, Toast.LENGTH_LONG).show();
                finish();

            }

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                //If we are here, everything processed successfully and we have an Uri data
                Uri mProductPhotoUri = data.getData();
                mCurrentPhotoUri = mProductPhotoUri.toString();

                //We use Glide to import photo images
                Glide.with(this).load(mProductPhotoUri)
                        .placeholder(R.drawable.ic_insert_photo)
                        .crossFade()
                        .fitCenter()
                        .into(productImage);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //We got a GO from the user
            getImageFromGallaray();
        } else {
            Toast.makeText(this, "ERROR 50000", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                mCurrentProductUri,
                PRODUCT_COLS,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            int i_COL_NAME = 1;
            int i_COL_QUANTITY = 2;
            int i_COL_PRICE = 3;
            int i_COL_ITEMS_SOLD = 4;
            int i_COL_SUPPLIER = 5;
            int i_COL_PICTURE = 6;

            // Extract values from current cursor
            String name = cursor.getString(i_COL_NAME);
            int quantity = cursor.getInt(i_COL_QUANTITY);
            float price = cursor.getFloat(i_COL_PRICE);
            int itemSold = cursor.getInt(i_COL_ITEMS_SOLD);
            String supplier = cursor.getString(i_COL_SUPPLIER);
            mCurrentPhotoUri = cursor.getString(i_COL_PICTURE);


            //We updates fields to values on DB
            produuctName.setText(name);
            setTitle(name);
            productPrice.setText(String.valueOf(price));
            productQuantity.setText(String.valueOf(quantity));
            productSold.setText(String.valueOf(itemSold));
            suppliersNumber.setText(supplier);
            //Update photo using Glide
            Glide.with(this).load(mCurrentPhotoUri)
                    .placeholder(R.drawable.ic_insert_photo)
                    .error(R.drawable.ic_insert_photo)
                    .crossFade()
                    .fitCenter()
                    .into(productImage);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        produuctName.setText("");
        productPrice.setText("");
        productQuantity.setText("");
        productSold.setText("");
        suppliersNumber.setText("");

    }


    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
