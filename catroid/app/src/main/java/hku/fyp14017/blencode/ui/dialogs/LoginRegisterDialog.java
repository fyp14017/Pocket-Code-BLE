/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.fyp14017.hku/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.fyp14017.hku/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.hku/licenses/>.
 */
package hku.fyp14017.blencode.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.transfers.RegistrationTask;
import hku.fyp14017.blencode.transfers.RegistrationTask.OnRegistrationCompleteListener;
import hku.fyp14017.blencode.ui.MainMenuActivity;
import hku.fyp14017.blencode.web.ServerCalls;

import hku.fyp14017.blencode.common.Constants;

public class LoginRegisterDialog extends DialogFragment implements OnRegistrationCompleteListener {

	public static final String PASSWORD_FORGOTTEN_PATH = "catroid/passwordrecovery?username=";
	public static final String DIALOG_FRAGMENT_TAG = "dialog_login_register";

	private EditText usernameEditText;
	private EditText passwordEditText;
	private TextView termsOfUseLinkTextView;

	@Override
	public Dialog onCreateDialog(Bundle bundle) {
		View rootView = LayoutInflater.from(getActivity()).inflate(hku.fyp14017.blencode.R.layout.dialog_login_register, null);

		usernameEditText = (EditText) rootView.findViewById(hku.fyp14017.blencode.R.id.username);
		passwordEditText = (EditText) rootView.findViewById(hku.fyp14017.blencode.R.id.password);
		termsOfUseLinkTextView = (TextView) rootView.findViewById(hku.fyp14017.blencode.R.id.register_terms_link);

		String termsOfUseUrl = getString(hku.fyp14017.blencode.R.string.about_link_template, Constants.CATROBAT_TERMS_OF_USE_URL,
				getString(hku.fyp14017.blencode.R.string.register_pocketcode_terms_of_use_text));
		termsOfUseLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
		termsOfUseLinkTextView.setText(Html.fromHtml(termsOfUseUrl));

		usernameEditText.setText("");
		passwordEditText.setText("");

		final AlertDialog loginRegisterDialog = new AlertDialog.Builder(getActivity()).setView(rootView)
				.setTitle(hku.fyp14017.blencode.R.string.login_register_dialog_title).setPositiveButton(hku.fyp14017.blencode.R.string.login_or_register, null)
				.setNeutralButton(hku.fyp14017.blencode.R.string.password_forgotten, null).create();
		loginRegisterDialog.setCanceledOnTouchOutside(true);
		loginRegisterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		loginRegisterDialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(usernameEditText, InputMethodManager.SHOW_IMPLICIT);

				Button loginRegisterButton = loginRegisterDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				loginRegisterButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						handleLoginRegisterButtonClick();
					}
				});

				Button passwordFhkuottenButton = loginRegisterDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				passwordFhkuottenButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						handlePasswordForgottenButtonClick();
					}
				});
			}
		});

		return loginRegisterDialog;
	}

	@Override
	public void onRegistrationComplete() {
		dismiss();

		UploadProjectDialog uploadProjectDialog = new UploadProjectDialog();
		uploadProjectDialog.show(getFragmentManager(), UploadProjectDialog.DIALOG_FRAGMENT_TAG);
	}

	private void handleLoginRegisterButtonClick() {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		RegistrationTask registrationTask = new RegistrationTask(getActivity(), username, password);
		registrationTask.setOnRegistrationCompleteListener(this);
		registrationTask.execute();
	}

	private void handlePasswordForgottenButtonClick() {
		String username = usernameEditText.getText().toString();
		String baseUrl = ServerCalls.useTestUrl ? ServerCalls.BASE_URL_TEST_HTTP : Constants.BASE_URL_HTTPS;
		String url = baseUrl + PASSWORD_FORGOTTEN_PATH + username;

		((MainMenuActivity) getActivity()).startWebViewActivity(url,"not important");
	}
}
