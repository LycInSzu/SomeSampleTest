/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dialer.app.calllog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dialer.app.R;
import com.android.dialer.app.calllog.calllogcache.CallLogCache;
import com.android.dialer.calllogutils.PhoneCallDetails;
import com.android.dialer.common.LogUtil;
import com.android.dialer.compat.android.provider.VoicemailCompat;
import com.android.dialer.compat.telephony.TelephonyManagerCompat;
import com.android.dialer.logging.ContactSource;
import com.android.dialer.oem.MotorolaUtils;
import com.android.dialer.phonenumberutil.PhoneNumberHelper;
import com.android.dialer.storage.StorageComponent;
import com.android.dialer.util.DialerUtils;
import com.android.voicemail.VoicemailClient;
import com.android.voicemail.VoicemailComponent;
import com.android.voicemail.impl.transcribe.TranscriptionRatingHelper;
import com.google.internal.communications.voicemailtranscription.v1.TranscriptionRatingValue;
import com.mediatek.dialer.compat.ContactsCompat.PhoneCompat;
import com.mediatek.dialer.ext.ExtensionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/*PRIZE-add-yuandailin-2016-3-17-start*/
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;
//prize add by liyuchong, modify phone number show order in arabic language ,20190124-begin
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//prize add by liyuchong, modify phone number show order in arabic language ,20190124-end
//import android.telephony.SubscriptionManager;
/*PRIZE-add-yuandailin-2016-3-17-end*/

/**
 * Helper class to fill in the views in {@link PhoneCallDetailsViews}.
 */
public class PhoneCallDetailsHelper
        implements TranscriptionRatingHelper.SuccessListener,
        TranscriptionRatingHelper.FailureListener {
    /**
     * The maximum number of icons will be shown to represent the call types in a group.
     */
    private static final int MAX_CALL_TYPE_ICONS = 1;//PRIZE-change-yuandailin-2016-7-18

    private static final String PREF_VOICEMAIL_DONATION_PROMO_SHOWN_KEY =
            "pref_voicemail_donation_promo_shown_key";

    private final Context context;
    private final Resources resources;
    private final CallLogCache callLogCache;
    /**
     * Calendar used to construct dates
     */
    private final Calendar calendar;
    /**
     * The injected current time in milliseconds since the epoch. Used only by tests.
     */
    private Long currentTimeMillisForTest;

    private CharSequence phoneTypeLabelForTest;
    /**
     * List of items to be concatenated together for accessibility descriptions
     */
    private ArrayList<CharSequence> descriptionItems = new ArrayList<>();

    /**
     * Creates a new instance of the helper.
     *
     * <p>Generally you should have a single instance of this helper in any context.
     *
     * @param resources used to look up strings
     */
    public PhoneCallDetailsHelper(Context context, Resources resources, CallLogCache callLogCache) {
        this.context = context;
        this.resources = resources;
        this.callLogCache = callLogCache;
        calendar = Calendar.getInstance();
    }

    /**
     * Fills the call details views with content.
     */
    public void setPhoneCallDetails(PhoneCallDetailsViews views, PhoneCallDetails details) {
        // Display up to a given number of icons.
        views.callTypeIcons.clear();
        //views.callTypeIcons.setPhoneAccountHandle(details.accountHandle);//PRIZE-remove-yuandailin-2016-9-5
        views.callTypeIcons.setSlotIdAndSimCount(details.slotId, details.mSimCount);
        //views.callTypeIcons.setPrimaryCallTypeIcon(false);//PRIZE-run smoohtlier in callllog -yuandailin-2016-9-21
        int count = details.callTypes.length;
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*boolean isVoicemail = false;*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        for (int index = 0; index < count && index < MAX_CALL_TYPE_ICONS; ++index) {
            views.callTypeIcons.add(details.callTypes[index]);
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*if (index == 0) {
                isVoicemail = details.callTypes[index] == Calls.VOICEMAIL_TYPE;
            }*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        }

        // Show the video icon if the call had video enabled.
    /*views.callTypeIcons.setShowVideo(
        (details.features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO);*/
        views.callTypeIcons.setShowVideo(false);
        views.callTypeIcons.setShowHd(
                (details.features & Calls.FEATURES_HD_CALL) == Calls.FEATURES_HD_CALL);
        views.callTypeIcons.setShowWifi(
                MotorolaUtils.shouldShowWifiIconInCallLog(context, details.features));
        views.callTypeIcons.setShowAssistedDialed(
                (details.features & TelephonyManagerCompat.FEATURES_ASSISTED_DIALING)
                        == TelephonyManagerCompat.FEATURES_ASSISTED_DIALING);
        views.callTypeIcons.requestLayout();
        views.callTypeIcons.setVisibility(View.VISIBLE);

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        views.prize_call_types_icons.setPrimaryCallTypeIcon(false);
        views.prize_call_types_icons.clear();
        views.prize_call_types_icons.setSlotIdAndSimCount(details.slotId, details.mSimCount);
        for (int index = 0; index < count && index < MAX_CALL_TYPE_ICONS; ++index) {
            views.prize_call_types_icons.add(details.callTypes[index]);
        }
        // Show the video icon if the call had video enabled.
        views.prize_call_types_icons.setShowVideo(
                (details.features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO);
        ///M: Plug-in call to show different icons VoLTE, VoWifi, ViWifi in call logs
        ExtensionManager.getCallLogExtension().setShowVolteWifi(
                views.prize_call_types_icons, details.features);
        views.prize_call_types_icons.requestLayout();
        views.prize_call_types_icons.setVisibility(View.VISIBLE);
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        // Show the total call count only if there are more than the maximum number of icons.
        /*PRIZE-change-yuandailin-2016-3-17-start*/
        /*final Integer callCount;
        if (count > MAX_CALL_TYPE_ICONS) {
            callCount = count;
        } else {
            callCount = null;
        }*/

        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*final Integer callCount = count;
        // Set the call count, location, date and if voicemail, set the duration.
        setDetailText(views, callCount, details);*/
        CharSequence dateText = getCallDate(details);
        if (dateText != null) views.callLocationAndDate.setText(dateText);
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        // Set the account label if it exists.
        /*String accountLabel = mCallLogCache.getAccountLabel(details.accountHandle);
        if (!TextUtils.isEmpty(details.viaNumber)) {
            if (!TextUtils.isEmpty(accountLabel)) {
                accountLabel = mResources.getString(R.string.call_log_via_number_phone_account,
                        accountLabel, details.viaNumber);
            } else {
                accountLabel = mResources.getString(R.string.call_log_via_number,
                        details.viaNumber);
            }
        }
        if (!TextUtils.isEmpty(accountLabel)) {
            //views.callAccountLabel.setVisibility(View.VISIBLE);
            views.callAccountLabel.setVisibility(View.GONE);
            views.callAccountLabel.setText(accountLabel);
            *//*int color = mCallLogCache.getAccountColor(details.accountHandle);
            if (color == PhoneAccount.NO_HIGHLIGHT_COLOR) {
                int defaultColor = R.color.dialtacts_secondary_text_color;
                views.callAccountLabel.setTextColor(mContext.getResources().getColor(defaultColor));
            } else {
                views.callAccountLabel.setTextColor(color);
            }*//*
        } else {
            views.callAccountLabel.setVisibility(View.GONE);
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        //prize add by lijimeng callLog location 20181112-start
        CharSequence callLocationAndDate = getCallLocationAndDate(details);
        if (callLocationAndDate != null) {
            views.callLocation.setText(callLocationAndDate);
        }
        //prize add by lijimeng callLog location 20181112-end
        CharSequence nameText;
        final CharSequence displayNumber = details.displayNumber;
        String countString = "";
        //prize added by lijimeng,display groupeSize,20190308-start
        if(count > 1){
            countString = " (" + count + ")";
        }
        //prize added by lijimeng,display groupeSize,20190308-end
        if (TextUtils.isEmpty(details.getPreferredName())) {
            nameText = "";
            // We have a real phone number as "nameView" so make it always LTR
            views.nameView.setTextDirection(View.TEXT_DIRECTION_LTR);
            views.numberView.setText(displayNumber + countString);
          //  views.numberView.setText(R.string.prize_call_log_list_item_unknown);//PRIZE-change-yuandailin-2016-7-18
        } else {
            nameText = details.getPreferredName() + countString;/*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24*/
            views.numberView.setText("");
            views.callLocation.setText(displayNumber);//PRIZE-add-yuandailin-2016-7-18
        }

		
		//prize add by liyuchong, modify phone number show order in arabic language ,20190124-begin
        if (needLtrDirection(nameText.toString())) {
            nameText="\u202D" +nameText + "\u202C";
        }
       //prize add by liyuchong, modify phone number show order in arabic language ,20190124-end
		
        views.nameView.setText(nameText);
        if(PhoneNumberUtils.isEmergencyNumber((String) displayNumber)){
            views.numberView.setText("");
            //prize modified by lijimeng,display groupeSize,20190308-start
            //views.nameView.setText(context.getResources().getText(R.string.prize_emergency_title));
            views.nameView.setText(context.getResources().getText(R.string.prize_emergency_title) + countString);
            //prize modified by lijimeng,display groupeSize,20190308-end
            views.callLocation.setText(displayNumber);
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        if ((views.callLocation.getText() + "").trim().equals("")) {
            views.callLocation.setText(context.getResources().getString(R.string.prize_call_log_list_item_unknown));
        }
        views.nameView.setTextColor(context.getResources().getColor(R.color.prize_dialer_call_log_list_item_name_color));
        views.numberView.setTextColor(context.getResources().getColor(R.color.prize_dialer_call_log_list_item_name_color));
        if (details.callTypes[0] == Calls.MISSED_TYPE
                || details.callTypes[0] == Calls.REJECTED_TYPE) {
            views.nameView.setTextColor(context.getResources().getColor(R.color.prize_dialer_call_log_list_item_missed_name_color));
            views.numberView.setTextColor(context.getResources().getColor(R.color.prize_dialer_call_log_list_item_missed_name_color));
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    /*if (isVoicemail) {
      int relevantLinkTypes = Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS;
      views.voicemailTranscriptionView.setAutoLinkMask(relevantLinkTypes);

      String transcript = "";
      String branding = "";
      if (!TextUtils.isEmpty(details.transcription)) {
        transcript = details.transcription;

        if (details.transcriptionState == VoicemailCompat.TRANSCRIPTION_AVAILABLE
            || details.transcriptionState == VoicemailCompat.TRANSCRIPTION_AVAILABLE_AND_RATED) {
          branding = resources.getString(R.string.voicemail_transcription_branding_text);
        }
      } else {
        switch (details.transcriptionState) {
          case VoicemailCompat.TRANSCRIPTION_IN_PROGRESS:
            branding = resources.getString(R.string.voicemail_transcription_in_progress);
            break;
          case VoicemailCompat.TRANSCRIPTION_FAILED_NO_SPEECH_DETECTED:
            branding = resources.getString(R.string.voicemail_transcription_failed_no_speech);
            break;
          case VoicemailCompat.TRANSCRIPTION_FAILED_LANGUAGE_NOT_SUPPORTED:
            branding =
                resources.getString(R.string.voicemail_transcription_failed_language_not_supported);
            break;
          case VoicemailCompat.TRANSCRIPTION_FAILED:
            branding = resources.getString(R.string.voicemail_transcription_failed);
            break;
          default:
            break; // Fall through
        }
      }

      views.voicemailTranscriptionView.setText(transcript);
      views.voicemailTranscriptionBrandingView.setText(branding);

      View ratingView = views.voicemailTranscriptionRatingView;
      if (shouldShowTranscriptionRating(details.transcriptionState, details.accountHandle)) {
        ratingView.setVisibility(View.VISIBLE);
        ratingView
            .findViewById(R.id.voicemail_transcription_rating_good)
            .setOnClickListener(
                view ->
                    recordTranscriptionRating(
                        TranscriptionRatingValue.GOOD_TRANSCRIPTION, details, ratingView));
        ratingView
            .findViewById(R.id.voicemail_transcription_rating_bad)
            .setOnClickListener(
                view ->
                    recordTranscriptionRating(
                        TranscriptionRatingValue.BAD_TRANSCRIPTION, details, ratingView));
      } else {
        ratingView.setVisibility(View.GONE);
      }
    }*/

        // Bold if not read
        //prize delete by lijimeng 20181102-start
        /*Typeface typeface = details.isRead ? Typeface.SANS_SERIF : Typeface.DEFAULT_BOLD;
        views.nameView.setTypeface(typeface);
        //views.voicemailTranscriptionView.setTypeface(typeface);
        //views.voicemailTranscriptionBrandingView.setTypeface(typeface);
        views.callLocationAndDate.setTypeface(typeface);
        views.callLocationAndDate.setTextColor(
                ContextCompat.getColor(
                        context,
                        details.isRead ? R.color.call_log_detail_color : R.color.call_log_unread_text_color));*/
        //prize delete by lijimeng 20181102-start
    }
	//prize add by liyuchong, modify phone number show order in arabic language ,20190124-begin
	  private boolean needLtrDirection(String text){
      if(TextUtils.isEmpty(text)){
          return false;
      }
		String pattern = "(^\\+|^)[0-9\\(\\) -]{3,}($)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        if (m.find()) {
			return true;
        }
		return false;
  }
	//prize add by liyuchong, modify phone number show order in arabic language ,20190124-end
    private boolean shouldShowTranscriptionRating(
            int transcriptionState, PhoneAccountHandle account) {
        if (transcriptionState != VoicemailCompat.TRANSCRIPTION_AVAILABLE) {
            return false;
        }

        VoicemailClient client = VoicemailComponent.get(context).getVoicemailClient();
        if (client.isVoicemailDonationEnabled(context, account)) {
            return true;
        }

        // Also show the rating option if voicemail transcription is available (but not enabled)
        // and the donation promo has not yet been shown.
        if (client.isVoicemailDonationAvailable(context) && !hasSeenVoicemailDonationPromo(context)) {
            return true;
        }

        return false;
    }

    private void recordTranscriptionRating(
            TranscriptionRatingValue ratingValue, PhoneCallDetails details, View ratingView) {
        LogUtil.enterBlock("PhoneCallDetailsHelper.recordTranscriptionRating");

        if (shouldShowVoicemailDonationPromo(context)) {
            showVoicemailDonationPromo(ratingValue, details, ratingView);
        } else {
            TranscriptionRatingHelper.sendRating(
                    context,
                    ratingValue,
                    Uri.parse(details.voicemailUri),
                    this::onRatingSuccess,
                    this::onRatingFailure);
        }
    }

    static boolean shouldShowVoicemailDonationPromo(Context context) {
        VoicemailClient client = VoicemailComponent.get(context).getVoicemailClient();
        return client.isVoicemailTranscriptionAvailable(context)
                && client.isVoicemailDonationAvailable(context)
                && !hasSeenVoicemailDonationPromo(context);
    }

    static boolean hasSeenVoicemailDonationPromo(Context context) {
        return StorageComponent.get(context.getApplicationContext())
                .unencryptedSharedPrefs()
                .getBoolean(PREF_VOICEMAIL_DONATION_PROMO_SHOWN_KEY, false);
    }

    private void showVoicemailDonationPromo(
            TranscriptionRatingValue ratingValue, PhoneCallDetails details, View ratingView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getVoicemailDonationPromoContent());
        builder.setPositiveButton(
                R.string.voicemail_donation_promo_opt_in,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int button) {
                        LogUtil.i("PhoneCallDetailsHelper.showVoicemailDonationPromo", "onClick");
                        dialog.cancel();
                        recordPromoShown(context);
                        VoicemailComponent.get(context)
                                .getVoicemailClient()
                                .setVoicemailDonationEnabled(context, details.accountHandle, true);
                        TranscriptionRatingHelper.sendRating(
                                context,
                                ratingValue,
                                Uri.parse(details.voicemailUri),
                                PhoneCallDetailsHelper.this::onRatingSuccess,
                                PhoneCallDetailsHelper.this::onRatingFailure);
                        ratingView.setVisibility(View.GONE);
                    }
                });
        builder.setNegativeButton(
                R.string.voicemail_donation_promo_opt_out,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int button) {
                        dialog.cancel();
                        recordPromoShown(context);
                        ratingView.setVisibility(View.GONE);
                    }
                });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        // Use a custom title to prevent truncation, sigh
        TextView title = new TextView(context);
        title.setText(R.string.voicemail_donation_promo_title);

        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTextColor(ContextCompat.getColor(context, R.color.dialer_primary_text_color));
        title.setPadding(
                dpsToPixels(context, 24), /* left */
                dpsToPixels(context, 10), /* top */
                dpsToPixels(context, 24), /* right */
                dpsToPixels(context, 0)); /* bottom */
        dialog.setCustomTitle(title);

        dialog.show();

        // Make the message link clickable and adjust the appearance of the message and buttons
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setLineSpacing(0, 1.2f);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(
                    context
                            .getResources()
                            .getColor(R.color.voicemail_donation_promo_positive_button_text_color));
        }
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(
                    context
                            .getResources()
                            .getColor(R.color.voicemail_donation_promo_negative_button_text_color));
        }
    }

    private static int dpsToPixels(Context context, int dps) {
        return (int)
                (TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics()));
    }

    private static void recordPromoShown(Context context) {
        StorageComponent.get(context.getApplicationContext())
                .unencryptedSharedPrefs()
                .edit()
                .putBoolean(PREF_VOICEMAIL_DONATION_PROMO_SHOWN_KEY, true)
                .apply();
    }

    private SpannableString getVoicemailDonationPromoContent() {
        CharSequence content = context.getString(R.string.voicemail_donation_promo_content);
        CharSequence learnMore = context.getString(R.string.voicemail_donation_promo_learn_more);
        String learnMoreUrl = context.getString(R.string.voicemail_donation_promo_learn_more_url);
        SpannableString span = new SpannableString(content + " " + learnMore);
        int end = span.length();
        int start = end - learnMore.length();
        span.setSpan(new URLSpan(learnMoreUrl), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(
                new TextAppearanceSpan(context, R.style.PromoLinkStyle),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    @Override
    public void onRatingSuccess(Uri voicemailUri) {
        LogUtil.enterBlock("PhoneCallDetailsHelper.onRatingSuccess");
        Toast toast =
                Toast.makeText(context, R.string.voicemail_transcription_rating_thanks, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 50);
        toast.show();
    }

    @Override
    public void onRatingFailure(Throwable t) {
        LogUtil.e("PhoneCallDetailsHelper.onRatingFailure", "failed to send rating", t);
    }

    /**
     * Builds a string containing the call location and date. For voicemail logs only the call date is
     * returned because location information is displayed in the call action button
     *
     * @param details The call details.
     * @return The call location and date string.
     */
    public CharSequence getCallLocationAndDate(PhoneCallDetails details) {
        descriptionItems.clear();

        if (details.callTypes[0] != Calls.VOICEMAIL_TYPE) {
            // Get type of call (ie mobile, home, etc) if known, or the caller's location.
            CharSequence callTypeOrLocation = getCallTypeOrLocation(details);

            // Only add the call type or location if its not empty.  It will be empty for unknown
            // callers.
            /** M: [VoLTE ConfCallLog] the conference call only show date not show location or label  */
            if (!TextUtils.isEmpty(callTypeOrLocation) && details.conferenceId <= 0) {
                descriptionItems.add(callTypeOrLocation);
            }
        }

        // The date of this call
        //prize delete by lijimeng change calllog 20181102-start
        // descriptionItems.add(getCallDate(details));
        //prize delete by lijimeng change calllog 20181102-end
        // Create a comma separated list from the call type or location, and call date.
        return DialerUtils.join(descriptionItems);
    }

    /**
     * For a call, if there is an associated contact for the caller, return the known call type (e.g.
     * mobile, home, work). If there is no associated contact, attempt to use the caller's location if
     * known.
     *
     * @param details Call details to use.
     * @return Type of call (mobile/home) if known, or the location of the caller (if known).
     */
    public CharSequence getCallTypeOrLocation(PhoneCallDetails details) {
        if (details.callTypeOrLocation != null) {
            return details.callTypeOrLocation;
        }

        if (details.isSpam) {
            details.callTypeOrLocation = resources.getString(R.string.spam_number_call_log_label);
            return details.callTypeOrLocation;
        } else if (details.isBlocked) {
            details.callTypeOrLocation = resources.getString(R.string.blocked_number_call_log_label);
            return details.callTypeOrLocation;
        }

        CharSequence numberFormattedLabel = null;
        // Only show a label if the number is shown and it is not a SIP address.
        if (!TextUtils.isEmpty(details.number)
                && !PhoneNumberHelper.isUriNumber(details.number.toString())
                && !callLogCache.isVoicemailNumber(details.accountHandle, details.number)) {
            if (shouldShowLocation(details)) {
                numberFormattedLabel = details.geocode;
            } else if (!(details.numberType == Phone.TYPE_CUSTOM
                    && TextUtils.isEmpty(details.numberLabel))) {
                // Get type label only if it will not be "Custom" because of an empty number label.
                /*M: use  PhoneCompat to get lable; for AAS feature*/
                numberFormattedLabel =
                        phoneTypeLabelForTest != null
                                ? phoneTypeLabelForTest
                                : PhoneCompat.getTypeLabel(context, details.numberType, details.numberLabel);
            }
        }

        if (!TextUtils.isEmpty(details.namePrimary) && TextUtils.isEmpty(numberFormattedLabel)) {
            numberFormattedLabel = details.displayNumber;
        }
        if (numberFormattedLabel != null) {
            details.callTypeOrLocation = numberFormattedLabel.toString();
        }
        return numberFormattedLabel;
    }

    /**
     * Returns true if primary name is empty or the data is from Cequint Caller ID.
     */
    private static boolean shouldShowLocation(PhoneCallDetails details) {
        if (TextUtils.isEmpty(details.geocode)) {
            return false;
        }
        // For caller ID provided by Cequint we want to show the geo location.
        if (details.sourceType == ContactSource.Type.SOURCE_TYPE_CEQUINT_CALLER_ID) {
            return true;
        }
        // Don't bother showing geo location for contacts.
        if (!TextUtils.isEmpty(details.namePrimary)) {
            return false;
        }
        return true;
    }

    public void setPhoneTypeLabelForTest(CharSequence phoneTypeLabel) {
        this.phoneTypeLabelForTest = phoneTypeLabel;
    }

    /**
     * Get the call date/time of the call. For the call log this is relative to the current time. e.g.
     * 3 minutes ago. For voicemail, see {@link #getGranularDateTime(PhoneCallDetails)}
     *
     * @param details Call details to use.
     * @return String representing when the call occurred.
     */
    public CharSequence getCallDate(PhoneCallDetails details) {
        if (details.callTypes[0] == Calls.VOICEMAIL_TYPE) {
            return getGranularDateTime(details);
        }

       /* return DateUtils.getRelativeTimeSpanString(
                details.date,
                getCurrentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE);*/
        return formatTimeStampStringExtend(context, details.date);
    }

    /*PRIZE-add-yuandailin-2016-3-17-start*/
    public static String formatTimeStampStringExtend(Context context, long when) {
        Time then = new Time();
        int format_flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
        then.set(when);
        Time now = new Time();
        now.setToNow();
        if (then.yearDay == now.yearDay) {
            format_flags = DateUtils.FORMAT_SHOW_TIME;
        } else {
            format_flags = DateUtils.FORMAT_SHOW_DATE;
        }
        return DateUtils.formatDateTime(context, when, format_flags);
    }
    /*PRIZE-add-yuandailin-2016-3-17-end*/

    /**
     * Get the granular version of the call date/time of the call. The result is always in the form
     * 'DATE at TIME'. The date value changes based on when the call was created.
     *
     * <p>If created today, DATE is 'Today' If created this year, DATE is 'MMM dd' Otherwise, DATE is
     * 'MMM dd, yyyy'
     *
     * <p>TIME is the localized time format, e.g. 'hh:mm a' or 'HH:mm'
     *
     * @param details Call details to use
     * @return String representing when the call occurred
     */
    public CharSequence getGranularDateTime(PhoneCallDetails details) {
        return resources.getString(
                R.string.voicemailCallLogDateTimeFormat,
                getGranularDate(details.date),
                DateUtils.formatDateTime(context, details.date, DateUtils.FORMAT_SHOW_TIME));
    }

    /**
     * Get the granular version of the call date. See {@link #getGranularDateTime(PhoneCallDetails)}
     */
    private String getGranularDate(long date) {
        if (DateUtils.isToday(date)) {
            return resources.getString(R.string.voicemailCallLogToday);
        }
        return DateUtils.formatDateTime(
                context,
                date,
                DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_ABBREV_MONTH
                        | (shouldShowYear(date) ? DateUtils.FORMAT_SHOW_YEAR : DateUtils.FORMAT_NO_YEAR));
    }

    /**
     * Determines whether the year should be shown for the given date
     *
     * @return {@code true} if date is within the current year, {@code false} otherwise
     */
    private boolean shouldShowYear(long date) {
        calendar.setTimeInMillis(getCurrentTimeMillis());
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(date);
        return currentYear != calendar.get(Calendar.YEAR);
    }

    /**
     * Sets the text of the header view for the details page of a phone call.
     */
    public void setCallDetailsHeader(TextView nameView, PhoneCallDetails details) {
        final CharSequence nameText;
        if (!TextUtils.isEmpty(details.namePrimary)) {
            nameText = details.namePrimary;
        } else if (!TextUtils.isEmpty(details.displayNumber)) {
            nameText = details.displayNumber;
        } else {
            nameText = resources.getString(R.string.unknown);
        }
		//prize add by liyuchong, modify phone number show order in arabic language ,20190124-begin
		final CharSequence nameText2;
	    if (needLtrDirection(nameText.toString())) {
            nameText2="\u202D" +nameText + "\u202C";
        }else{
			nameText2=nameText;
		}
        nameView.setText(nameText2);
		//prize add by liyuchong, modify phone number show order in arabic language ,20190124-end
    }

    public void setCurrentTimeForTest(long currentTimeMillis) {
        currentTimeMillisForTest = currentTimeMillis;
    }

    /**
     * Returns the current time in milliseconds since the epoch.
     *
     * <p>It can be injected in tests using {@link #setCurrentTimeForTest(long)}.
     */
    private long getCurrentTimeMillis() {
        if (currentTimeMillisForTest == null) {
            return System.currentTimeMillis();
        } else {
            return currentTimeMillisForTest;
        }
    }

    /**
     * Sets the call count, date, and if it is a voicemail, sets the duration.
     */
    private void setDetailText(
            PhoneCallDetailsViews views, Integer callCount, PhoneCallDetails details) {
        // Combine the count (if present) and the date.
        CharSequence dateText = details.callLocationAndDate;
        final CharSequence text;
        if (callCount != null) {
            text = resources.getString(R.string.call_log_item_count_and_date, callCount, dateText);
        } else {
            text = dateText;
        }

        if (details.callTypes[0] == Calls.VOICEMAIL_TYPE && details.duration > 0) {
            views.callLocationAndDate.setText(
                    resources.getString(
                            R.string.voicemailCallLogDateTimeFormatWithDuration,
                            text,
                            getVoicemailDuration(details)));
        } else {
            views.callLocationAndDate.setText(text);
        }
    }

    private String getVoicemailDuration(PhoneCallDetails details) {
        long minutes = TimeUnit.SECONDS.toMinutes(details.duration);
        long seconds = details.duration - TimeUnit.MINUTES.toSeconds(minutes);
        if (minutes > 99) {
            minutes = 99;
        }
        return resources.getString(R.string.voicemailDurationFormat, minutes, seconds);
    }
}
