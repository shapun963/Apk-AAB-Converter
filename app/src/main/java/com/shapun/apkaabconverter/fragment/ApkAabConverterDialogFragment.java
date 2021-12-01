package com.shapun.apkaabconverter.fragment;

import android.app.Dialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shapun.apkaabconverter.converter.AabToApkConverter;
import com.shapun.apkaabconverter.R;
import com.shapun.apkaabconverter.converter.ApkToAaabConverter;
import com.shapun.apkaabconverter.converter.Logger;
import com.shapun.apkaabconverter.util.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ApkAabConverterDialogFragment extends DialogFragment {
    public static final int APK_TO_AAB = 1;
    public static final int AAB_TO_APK = 2;
    private int mMode;
    private File mTempDir;
    private File mTempInputPath;
    private File mTempOutputPath;
	private Uri mInputUri;
	private Uri mOutputUri;
    private MaterialButton btn_convert;
	private TextInputEditText tiet_input_path;
	private TextInputEditText tiet_output_path;
	private TextInputLayout til_input_path;
	private TextInputLayout til_output_path;
    private final ActivityResultLauncher<String> mResultLauncherSelectOutput =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(),
                    result -> {
                        if (result != null) {
							ContentResolver contentResolver = requireContext().getContentResolver();
							String name = Utils.queryName(contentResolver,result);
							if(mMode == AAB_TO_APK){
								if(!name.endsWith(".apks")){
									Utils.toast(requireContext(),"File name must end with .apks");
									return;	
								}
							}else{
								if(!name.endsWith(".aab")){
									Utils.toast(requireContext(),"File name must end with .aab");
									return;
								}
							}
							mOutputUri = result;
							tiet_output_path.setText(name);                 
                        }
                    });
    private final ActivityResultLauncher<String> mResultLauncherSelectInput =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result != null) {
                            ContentResolver cr = requireContext().getContentResolver();
							String name = Utils.queryName(cr, result);
							mInputUri = result;
							if(mMode == AAB_TO_APK){
								if(!name.endsWith(".aab")){
									Utils.toast(requireContext(),"Selected file is not a aab file");
									return;
								}
							}else{
								if(!name.endsWith(".apk")){
									Utils.toast(requireContext(),"Selected file is not a apk file");
									return;
								}
							}
							tiet_input_path.setText(name);
                        }
                    });
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_apk_aab_converter, container, false);
        initializeViews(view);
        initializeLogic(savedInstanceState);
        return view;
    }

    private void initializeViews(View view) {
        btn_convert = view.findViewById(R.id.btn_convert);
		tiet_input_path = view.findViewById(R.id.tiet_input_path);
		tiet_output_path = view.findViewById(R.id.tiet_output_path);
		til_input_path = view.findViewById(R.id.til_input_path);
		til_output_path = view.findViewById(R.id.til_output_path);		
    }

    private void initializeLogic(Bundle savedInstanceState) {
		mMode = requireArguments().getInt("mode", AAB_TO_APK);
		mTempDir = new File(requireContext().getExternalFilesDir(null), "temp");
		mTempDir.mkdirs();
		mTempInputPath = new File(mTempDir,(mMode == AAB_TO_APK)?"input.aab":"input.apk");
		mTempOutputPath = new File(mTempDir,(mMode == AAB_TO_APK)?"output.apks":"output.aab");
        
        btn_convert.setOnClickListener(v -> {
			if(mInputUri == null){
				Utils.toast(requireContext(),"Input can't be empty");
				return;
			}
			if(mOutputUri == null){
				Utils.toast(requireContext(),"Output can't be empty");
				return;
			}
			try (InputStream is = getContentResolver().openInputStream(mInputUri);OutputStream os = new FileOutputStream(mTempInputPath)) {                          
                   copy(is,os);		   				   
				   setCancelable(false);
				   ViewGroup root = (ViewGroup)getView();
				   root.removeAllViews();
				   ProgressBar progressbar = new ProgressBar(requireContext());
				   root.addView(progressbar);
				   TextView tvLogs = new TextView(requireContext());
				   Utils.setPadding(tvLogs,(int)Utils.dpToPx(requireContext(),8));
				   tvLogs.setTextSize(18);
				   Logger logger = new Logger();
				   logger.setLogListener(log -> {
					   ContextCompat.getMainExecutor(requireContext()).execute(() -> {
						   tvLogs.setText(logger.getLogs());
						});
					});
					root.addView(tvLogs);
					Executors.newSingleThreadExecutor().execute(() -> {
						try{
							if (mMode == APK_TO_AAB) {
								ApkToAaabConverter apkToAabConverter = new ApkToAaabConverter.Builder(requireContext(),mTempInputPath,mTempOutputPath)
									.setLogger(logger)
									.build();
									apkToAabConverter.start();
							} else {
								AabToApkConverter apkToAabConverter =new AabToApkConverter.Builder(requireContext(),mTempInputPath,mTempOutputPath)
										.setLogger(logger)
										.build();
										apkToAabConverter.start();
							}
							try(InputStream is2 = new FileInputStream(mTempOutputPath);OutputStream os2 = getContentResolver().openOutputStream(mOutputUri)) {
							    copy(is2,os2);
							}
						}catch(Exception e){
							ContextCompat.getMainExecutor(requireContext()).execute(() -> {
								showErrorDialog(e.toString());
							});
						}
						ContextCompat.getMainExecutor(requireContext()).execute(() ->{
							root.removeView(progressbar);
							setCancelable(true);
						});
					});
			 } catch (IOException e) {
				 showErrorDialog(e.getMessage());
			 }
        });
				
		til_input_path.setEndIconOnClickListener(v->{
			mResultLauncherSelectInput.launch(("*/*"));
			til_input_path.requestFocus();
		});
		
		til_output_path.setEndIconOnClickListener(v->{
			ContentResolver cr = getContentResolver();
			String name = (mInputUri==null)?"unknown.???":Utils.queryName(cr,mInputUri);
			name = name.substring(0,name.lastIndexOf("."));
			if(mMode==APK_TO_AAB){
				mResultLauncherSelectOutput.launch(name+".aab");
			}else{
				mResultLauncherSelectOutput.launch(name+".apks");
			}
			til_output_path.requestFocus();
		}); 
    }

    private void showErrorDialog(String error){
		new MaterialAlertDialogBuilder(requireContext())
				.setTitle("Failed to convert file")
				.setMessage(error)
				.setPositiveButton("Cancel",null)
				.show();
	}
	
	private void copy(InputStream is,OutputStream os) throws IOException{
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) > 0) {
           os.write(buffer, 0, length);
        }
	}
	private ContentResolver getContentResolver(){
		return requireContext().getContentResolver();
	}

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
			Window window = dialog.getWindow();
			window.setBackgroundDrawable(null);
			WindowManager.LayoutParams attributes = window.getAttributes();
			window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
			int margin = Utils.dpToPx(requireContext(),20);
			FrameLayout.LayoutParams params = ((FrameLayout.LayoutParams)getView().getLayoutParams());
			params.setMargins(margin,margin,margin,margin);
        }
    }

    public static ApkAabConverterDialogFragment newInstance(int mode) {
        Bundle bundle = new Bundle();
        bundle.putInt("mode", mode);
        ApkAabConverterDialogFragment dialog = new ApkAabConverterDialogFragment();
        dialog.setArguments(bundle);
        return dialog;
    }
}
