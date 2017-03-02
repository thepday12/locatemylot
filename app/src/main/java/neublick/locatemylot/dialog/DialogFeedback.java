package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import static android.view.View.OnClickListener;

import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.util.Utils;

public class DialogFeedback extends Activity {

     private EditText message;
    private EditText name;
    private EditText email;
    private EditText phone;
	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

//		DisplayMetrics metrics = getResources().getDisplayMetrics();
//		int screenWidth = (int) (metrics.widthPixels * 0.80);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		setContentView(R.layout.dialog_feedback);
		// set below the setContentView()
//		getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

		Button sendFeedBack = (Button)findViewById(R.id.send_feedback);
		Button cancel = (Button)findViewById(R.id.action_cancel);

		 message 	= (EditText)findViewById(R.id.message);
		 name		= (EditText)findViewById(R.id.name);
		 email 		= (EditText)findViewById(R.id.email);
		 phone 		= (EditText)findViewById(R.id.phone);
phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == R.id.submit || id == EditorInfo.IME_NULL) {
            sendFeedBack();
            return true;
        }
        return false;
    }
});
		cancel.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				finish();
			}
		});

		sendFeedBack.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
                sendFeedBack();
			}
		});
	}


    private void sendFeedBack(){
        String tName 	= name.getText().toString().trim();
        String tEmail 	= email.getText().toString().trim();
        String tPhone	= phone.getText().toString().trim();
        String tMessage = message.getText().toString().trim();

        if (tMessage.equals("") || tName.equals("") || tEmail.equals("")) {
            new AlertDialog.Builder(DialogFeedback.this)
                    .setTitle("Notice")
                    .setMessage("Please fill all input")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }


        new SendFeedbackTask().execute(
                tName,
                tEmail,
                tPhone,
                tMessage
        );
        if (Utils.isInternetConnected(DialogFeedback.this)) {
            new AlertDialog.Builder(DialogFeedback.this)
                    .setTitle("LocateMyLot")
                    .setMessage("Thank you for sending feedback")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            DialogFeedback.this.finish();
                        }
                    })
                    .show();
        }
        else
        {
            new AlertDialog.Builder(DialogFeedback.this)
                    .setTitle("LocateMyLot")
                    .setMessage("Internet connection is not available. Could not send feedback.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }
	// cac tham so truyen vao execute() lan luot la name, email, phone, message
	class SendFeedbackTask extends AsyncTask<String, Void, String> {
		@Override public String doInBackground(String... args) {
			// name, email, phone, message
			String link = String.format(
					Config.CMS_URL + "/act.php?act=sendfeedback&name=%s&email=%s&phone=%s&message=%s",
				args[0],
				args[1],
				args[2],
				args[3]
			);

			URL url = null;
			try {
				url = new URL(link);
			} catch(MalformedURLException e) {

			}
			if (url == null) {
				return null;
			}
			BufferedReader reader = null;
			try {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				InputStreamReader bis = new InputStreamReader(conn.getInputStream());
				reader = new BufferedReader(bis);
				return reader.readLine();
			} catch(IOException e) {
				return null;
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch(IOException e) {

					}
				}
			}
		}
		@Override public void onPostExecute(String result) {

		}
	}
}