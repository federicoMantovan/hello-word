package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;

import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BillActivity extends AppCompatActivity {
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List<Scontrino> list = new LinkedList<Scontrino>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    static final int REQUEST_TAKE_PHOTO = 1;
    public static final int PICK_PHOTO_FOR_AVATAR = 2;
    String tempPhotoPath;
    public Button export;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        Intent intent = getIntent();
        Log.d("Memes", Variables.getInstance().getCurrentMissionDir());
        String missionName = intent.getExtras().getString("missionName");
        setTitle(missionName);
        initializeComponents();
    }

    /** Dal Maso
     *  Gestione delle animazioni e visualizzazione delle foto salvate precedentemente
     */
    public void initializeComponents(){
        printAllImages();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        export = (Button) findViewById(R.id.exp);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                animateFAB();
            }
        });
        //Camera button
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhotoIntent();
            }
        });
        //Gallery button
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    /** Dal Maso
     *  Animazioni per il Floating Action Button (FAB)
     */
    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }

    /**Dal Maso
     * Aggiunge una card alla lista
     * @param title Titolo della card (Nome del file)
     * @param desc Descrizione del file
     * @param img Bitmap della foto
     */
    public void addToList(String title, String desc, Bitmap img){
        list.add(new Scontrino(title, desc, img));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list);
        listView.setAdapter(adapter);
    }

    public void addToMissionGrid(String title, String desc, Bitmap img){
        list.add(new Scontrino(title, desc, img));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list);
        listView.setAdapter(adapter);
    }


    /**Lazzarin
     * It Opens the camera,takes the photo and puts as Extra Uri created by createImageFile method.
     * @Framing Camera, directory modified by createImageFile
     */
    private void takePhotoIntent() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {}
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhoto, REQUEST_TAKE_PHOTO);
            }
        }
    }


    /**Lazzarin
     * It creates a temporary file where to save the photo on.
     * @Framing Directory Pictures
     * @Return the temporary file
     *
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = new File(Variables.getInstance().getCurrentMissionDir());
        File image = File.createTempFile(
                "temp",  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        tempPhotoPath = image.getAbsolutePath();
        return image;
    }


    /** Dal Maso
     *  Cancella i file temporanei utilizzati per il salvataggio delle foto da fotocamera
     */
    public void deleteTempFiles(){
        File[] files = readAllImages();
        String filename = "";
        for (int i = 0; i < files.length; i++)
        {
            filename = files[i].getName();
            Log.d("Sub", filename.substring(0,4));
            if(filename.substring(0,4).equals("temp")){
                files[i].delete();
            }
        }
    }


    /** Dal Maso
     *  Selezione foto da galleria
     */
    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }


    /** Dal Maso
     * Cattura risultato degli intent
     * @param requestCode ritorna il numero di azione compiuta
     * @param resultCode indica se l'operazione è andata a buon fine
     * @param data Risultato dell'operazione
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                /**lazzarin
                 * Saves definitely the photo without losing quality, deletes the temporary file and shows
                 * the new photo.
                 * @Framing Add the photo on the directory using savePickedFile()
                 */
                case(REQUEST_TAKE_PHOTO):
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmapPhoto = BitmapFactory.decodeFile(tempPhotoPath,bmOptions);
                    savePickedFile(bitmapPhoto);
                    deleteTempFiles();
                    clearAllImages();
                    printAllImages();
                    break;

                //Dal Maso
                //Foto presa da galleria
                case (PICK_PHOTO_FOR_AVATAR):
                    photoURI = data.getData();
                    try {
                        Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                        savePickedFile(btm);
                        clearAllImages();
                        printAllImages();
                        Log.d("Foto da galleria", "OK");
                    }catch (Exception e){
                        Log.d("Foto da galleria", "ERROR");
                    }
                    break;
                //Dal Maso
                //Gestisco il risultato del Resize
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                    Log.d("Crop", "OK");
                    clearAllImages();
                    printAllImages();
                    break;
            }
        }
    }

    //
    //

    /** Dal Maso
     * Salva il bitmap passato nell'apposita cartella
     * @param imageToSave bitmap da salvare come jpeg
     */
    private void savePickedFile(Bitmap imageToSave) {
        String root = Variables.getInstance().getCurrentMissionDir();
        File myDir = new File(root);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String fname = imageFileName+".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //PICCOLO
            // aggiungo il file al db
            //DatabaseManager helper = DatabaseManager.getInstance(getApplicationContext());
            //helper.addPhoto(root+fname); DB ALTERNATIVO
            //DbManager db = new DbManager(getApplicationContext());
            //db.addRecord(root+fname,"","","");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** NOT USED
     * Lazzarin
     * @param  imageToCrop is the photo we want to resize. It has to be a Bitmap object.
     * @return an Uri object taken to the file allocated in "documents",so it isn't show on the gallery
     *
     */
    private Uri savePhotoForCrop (Bitmap imageToCrop) {
        File allocation = temporaryFile();
        try {
            FileOutputStream out = new FileOutputStream(allocation);
            imageToCrop.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri uri=Uri.fromFile(allocation);
        return uri;

    }

    /** NOT USED
     * Lazzarin
     * @return temporary allocation with a File object.
     */
    private File temporaryFile()
    {
        String root = Variables.getInstance().getCurrentMissionDir();
        File myDir = new File(root);
        String imageFileName = "photoToCrop.jpg";
        File file = new File(myDir, imageFileName);
        if (file.exists())
            file.delete();
        return file;
    }


    /**PICCOLO
     * Metodo che "ripulisce" lo schermo dalle immagini
     */
    public void clearAllImages(){
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list);
        adapter.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }//clearAllImages


    /** Dal Maso
     * Legge tutte le immagini
     * @return ritorna tutti i file letti nella cartella
     */
    private File[] readAllImages(){
        String path = Variables.getInstance().getCurrentMissionDir();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        return files;
    }


    /** Dal Maso
     *  Stampa tutte le immagini
     */
    public void printAllImages(){
        File[] files = readAllImages();
        for (int i = 0; i < files.length; i++)
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());
            addToList(files[i].getName(), "Descrizione della foto", myBitmap);
        }
    }


    /** Dal Maso
     *  Stampa l'ultima foto
     */
    private void printLastImage(){
        File[] files = readAllImages();
        Bitmap myBitmap = BitmapFactory.decodeFile(files[files.length-1].getAbsolutePath());
        addToList(files[files.length-1].getName(), "Descrizione della foto", myBitmap);
    }


    /** NOT USED
     * Dal Maso
     * Stampa il bitmap passato (Solo per testing)
     * @param myBitmap bitmap da stampare
     */
    private void printThisBitmap(Bitmap myBitmap){
        addToList("Print this bitmap", "Descrizione della foto", myBitmap);
    }


    /** NOT USED
     * PICCOLO_Edit by Dal Maso
     * Metodo che cancella l'i-esimo file in una directory
     * @param toDelete l'indice del file da cancellare
     * @param path percorso del file da cancellare
     * @return se l'operazione è andata a buon fine
     */
    public boolean deleteFile(int toDelete, String path){
        File directory = new File(path);
        File[] files = directory.listFiles();
        return files[toDelete].delete();
    }//deleteFile


    /**PICCOLO_Edit by Dal Maso
     * Metodo che cancella permette all'utente di ridimensionare la foto
     * @param toCrop l'indice della foto di cui fire il resize
     * @param path percorso della foto
     */
    public void cropFile(int toCrop, String path){
        Log.d("Crop","Success");
        boolean result = false;
        File directory = new File(path);
        File[] files = directory.listFiles();
        CropImage.activity(Uri.fromFile(files[toCrop])).start(this);
    }//cropFile

    /** NOT USED
     * VERSIONE DATABASE
     *PICCOLO
     * @param filename il id del file da cancellare a
     */
    /*
    private void deleteFileAndRow(String filename){
        DbManager db = new DbManager(getApplicationContext());
        //cancello il file associato solo se la query va a buon fine
        if(db.delete(filename)){
            File file = new File(filename);
            boolean deleted = file.delete();
        }//if
    }//deletePickedFile */

    /**
     * Federico
     * <p>
     * Call the camera, the method to merge and save the united bitmaps
     */
    private void mergePhoto() {
        takePhotoIntent();
        File[] photo = readAllImages();
        int secondLast = photo.length - 2;
        int lastPh = photo.length - 1;
        Bitmap f = BitmapFactory.decodeFile(photo[secondLast].getAbsolutePath());
        Bitmap s = BitmapFactory.decodeFile(photo[lastPh].getAbsolutePath());

        savePickedFile(merge(f, s));
    }

    /**
     * Federico
     * <p>
     * Method that combines two bitmaps
     *
     * @param f first bitmap to merge
     * @param s second bitmap to merge
     * @return bitmap formed by the union of two bitmaps passed as a parameter
     */
    private Bitmap merge(Bitmap f, Bitmap s) {
        Bitmap cs = null;

        int width, height = 0;

        if (f.getWidth() > s.getWidth()) {
            width = f.getWidth();
            height = f.getHeight() + s.getHeight();
        } else {
            width = s.getWidth();
            height = f.getHeight() + s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(f, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, f.getHeight(), null);

        return cs;
    }

    /*Federico
     *
     *---TO EXPORT MISSION USE THE EXPORT + COPY METHOD OR THE SAVE METHOD---
     */

    /**
     * Federico
     *
     * Method that exports all the photos
     * @throws IOException
     */
    private void export() throws IOException {
        File[] ph = readAllImages();
        //Destination file path
        File targetLocation = new File(Environment.getDataDirectory().toString());

        for(int i = 0; i < ph.length; i++){
            File sourceLocation = ph[i];
            copy(sourceLocation, targetLocation);
        }
    }

    /**
     * Federico
     *
     * Method that copies a folder with the files contained or individual files
     * @param sourceLocation file to move
     * @param targetLocation destination file
     * @throws IOException
     */
    private void copy(File sourceLocation , File targetLocation) throws IOException {
        //if you copy a folder
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            //list all the directory contents
            String[] files = sourceLocation.list();
            for (int i=0; i<files.length; i++) {
                copy(new File(sourceLocation, files[i]),
                        new File(targetLocation, files[i]));
            }
        }
        //if you copy a file
        else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    /**
     * Federico
     *
     * Method that exports all missions
     *(Imported external Apache Common IO library)
     */
    private void save(){
        //Destination path
        String exportDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File target = new File(exportDir, "AllMission");

        File[] source = readAllImages();
        for (int i = 0; i < source.length; i++){
            //if you copy a folder
            if(source[i].isDirectory()){
                try {
                    FileUtils.copyDirectory(source[i], target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //if you copy a file
            else{
                try {
                    FileUtils.copyFile(source[i], target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}