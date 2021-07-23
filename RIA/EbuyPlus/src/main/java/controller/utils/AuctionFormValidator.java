package controller.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import exception.file.ExceededFileSizeException;
import exception.file.FileEmptyException;
import exception.file.NoFileException;
import exception.file.NotAnImageIexeption;

/**
 * This class is responsible for the validation of all data related to the
 * client-side form used to create a new Auction.
 * 
 * It contains a value for each form field (which is filled oly if the corresponding
 * form field is valid) and also an error value for each (which is filled if the
 * corresponding form value is invalid).
 * 
 * Finally there is also a success message used to notify the client-side user when the
 * form is correctoly filled and the related Auction has been created successfully.
 */
public class AuctionFormValidator {
	private static final double NULL_DOUBLE = -1.0;
	private static final int NULL_INT = -1;

	// form fields
	@Nullable
	private String itemCode;
	@Nullable
	private String itemName;
	@Nullable
	private String itemDescription;
	@Nullable
	private String itemImageName;

	private double basePrice;
	private int minimumRise;
	@Nullable
	private LocalDateTime expireDateTime;

	// form errors
	@Nullable
	private String itemCodeError;
	@Nullable
	private String itemNameError;
	@Nullable
	private String itemDescriptionError;
	@Nullable
	private String itemImageError;

	@Nullable
	private String basePriceError;
	@Nullable
	private String minimumRiseError;
	@Nullable
	private String expireDateTimeError;
	
	// form success
	@Nullable
	private String successMessage;
	private int auctionId;

	private AuctionFormValidator() {
		this.itemName = null;
		this.itemCode = null;
		this.itemDescription = null;
		this.itemImageName = null;

		this.basePrice = NULL_DOUBLE;
		this.minimumRise = NULL_INT;
		this.expireDateTime = null;

		this.itemCodeError = null;
		this.itemNameError = null;
		this.itemDescriptionError = null;
		this.itemImageError = null;

		this.basePriceError = null;
		this.minimumRiseError = null;
		this.expireDateTimeError = null;
		
		this.successMessage = null;
		this.auctionId = NULL_INT;
	}

	@NonNull
	public static AuctionFormValidator getInstance() {
		return new AuctionFormValidator();
	}

	/**
	 * Validate all forms fields one by one.
	 */
	public void validate(@Nullable final String itemCodeStr, @Nullable final String itemNameStr, 
			@Nullable final String itemDecriptionStr, @Nullable final FileItem itemImage, 
			@Nullable final String basePriceStr, @Nullable final String minimumRiseStr, 
			@Nullable final String expireDateTimeStr) throws FileUploadException {
		
		validateItemCode(itemCodeStr);
		validateItemName(itemNameStr);
		validateItemDescription(itemDecriptionStr);
		validateItemImage(itemImage);

		validateBasePrice(basePriceStr);
		validateMinimumRise(minimumRiseStr);
		validateExpireDateTime(expireDateTimeStr);
	}

	private void validateItemCode(@Nullable final String itemCodeStr) {
		if (itemCodeStr == null || itemCodeStr.isBlank()) {
			this.itemCodeError = "Insert a valid code";
			return;
		}
		
		if (itemCodeStr.length() > Values.Item.MAX_CODE_LENGTH) {
			this.itemCodeError = "Code can't exceed " + Values.Item.MAX_CODE_LENGTH + " characters";
			return;
		}

		this.itemCode = itemCodeStr;
	}

	private void validateItemName(@Nullable final String itemNameStr) {
		if (itemNameStr == null || itemNameStr.isBlank()) {
			this.itemNameError = "Insert a valid name";
			return;
		}
		
		if (itemNameStr.length() > Values.Item.MAX_NAME_LENGTH) {
			this.itemNameError = "Name can't exceed " + Values.Item.MAX_NAME_LENGTH + " characters";
			return;
		}
		
		this.itemName = itemNameStr;
	}

	private void validateItemDescription(@Nullable final String itemDescriptionStr) {
		if (itemDescriptionStr == null || itemDescriptionStr.isBlank()) {
			this.itemDescriptionError = "Insert a description";
			return;
		}
		
		if (itemDescriptionStr.length() > Values.Item.MAX_DESCRIPTION_LENGTH) {
			this.itemDescriptionError = "Description can't exceed " + Values.Item.MAX_DESCRIPTION_LENGTH + " characters";
			return;
		}

		this.itemDescription = itemDescriptionStr;
	}
	
	private void validateItemImage(@Nullable final FileItem itemImage) throws FileUploadException {
		try {
			this.itemImageName = FileSaver.saveAndGet(Values.Params.IMAGE_FOLDER_PATH, itemImage);
		} catch (final NoFileException | FileEmptyException | ExceededFileSizeException | NotAnImageIexeption e) {
			this.itemImageError = e.getMessage();
		}
	}

	private void validateBasePrice(@Nullable final String basePriceStr) {
		if (basePriceStr == null || basePriceStr.isBlank()) {
			this.basePriceError = "Insert a valid base price";
			return;
		}
		
		try {
			final double basePrice = Double.parseDouble(basePriceStr);
			
			if (basePrice < Values.Auction.MIN_BASE_PRICE) {
				this.basePriceError = "Stating price must be at least " + Values.Auction.MIN_BASE_PRICE + " €";
				return;
			}
			
			this.basePrice = basePrice;
			
		} catch (final NumberFormatException | ClassCastException | NullPointerException e) {
			this.basePriceError = "Insert a valid base price";
		}
	}

	private void validateMinimumRise(@Nullable final String minimumRiseStr) {
		try {
			final int minimumRise = Integer.parseInt(minimumRiseStr);
			
			if (minimumRise < Values.Auction.MIN_MINIMUM_RISE) {
				this.minimumRiseError = "Minimum rise must be at least " + Values.Auction.MIN_MINIMUM_RISE + " €";
				return;
			}
			
			this.minimumRise = minimumRise;
			
		} catch (final NumberFormatException | ClassCastException | NullPointerException e) {
			this.minimumRiseError = "Insert a valid minimum rise";
		}
	}

	private void validateExpireDateTime(@Nullable final String expireDateTimeStr) {
		if (expireDateTimeStr == null || expireDateTimeStr.isBlank()) {
			this.expireDateTimeError = "Insert an expire date and time";
			return;
		}

		try {
			final LocalDateTime expireDateTime = LocalDateTime.parse(expireDateTimeStr);
			
			if (expireDateTime.isBefore(LocalDateTime.now())) {
				this.expireDateTimeError = "Can't accept values in the past for expire date and time";
				return;
			}
			
			this.expireDateTime = expireDateTime;
			
		} catch (final DateTimeParseException e) {
			this.expireDateTimeError = "Wrong format for expire date and time";
		}
	}

	@Nullable
	public String getItemCode() {
		return this.itemCode;
	}

	@Nullable
	public String getItemName() {
		return this.itemName;
	}
	
	@Nullable
	public String getItemDescription() {
		return this.itemDescription;
	}
	
	@Nullable
	public String getItemImageName() {
		return this.itemImageName;
	}

	public double getBasePrice() {
		return basePrice;
	}

	public int getMinimumRise() {
		return this.minimumRise;
	}

	@Nullable
	public LocalDateTime getExpireDateTime() {
		// to avoid the situation in which the user leave the date pre-filled by the form and, by the time he submits a new request, the pre-filled date is passed.
		if(this.expireDateTime != null && this.expireDateTime.isBefore(LocalDateTime.now()))
			this.expireDateTime = LocalDateTime.now();
		
		return this.expireDateTime;
	}

	@Nullable
	public String getItemCodeError() {
		return this.itemCodeError;
	}

	@Nullable
	public String getItemNameError() {
		return this.itemNameError;
	}

	@Nullable
	public String getItemDescriptionError() {
		return this.itemDescriptionError;
	}
	
	@Nullable
	public String getItemImageError() {
		return this.itemImageError;
	}

	@Nullable
	public String getBasePriceError() {
		return this.basePriceError;
	}

	@Nullable
	public String getMinimumRiseError() {
		return this.minimumRiseError;
	}

	@Nullable
	public String getExpireDateTimeError() {
		return this.expireDateTimeError;
	}
	
	
	public void setSuccessMessage(@NonNull final String successMessage) {
		this.successMessage = successMessage;
	}
	
	@Nullable
	public String getSuccessMessage() {
		return this.successMessage;
	}
	
	public void setAuctionId(final int auctionId) {
		this.auctionId = auctionId;
	}
	
	public int getAuctionId() {
		return this.auctionId;
	}

	/**
	 * A form is valid only if all fields are
	 * filled and all errors aren't set.
	 */
	public boolean isValidForm() {
		return this.itemCode != null && this.itemName != null
				&& this.itemDescription != null && this.itemImageName != null
				&& this.basePrice !=  NULL_DOUBLE && this.minimumRise != NULL_INT
				&& this.expireDateTime != null
				&& this.itemCodeError == null && this.itemNameError == null
				&& this.itemDescriptionError == null && this.itemImageError == null
				&& this.basePriceError == null && this.minimumRiseError == null
				&& this.expireDateTimeError == null;
	}
	
	@NonNull
	public String toString() {
		return "\n\n-----------------------------\nVALIDATOR:\n\n"
				+ "itemCode: " + this.itemCode
				+ "\nitemName: " + this.itemName
				+ "\nitemDescription: " + this.itemDescription
				+ "\nitemImageName: " + this.itemImageName
				+ "\n\nbasePrice: " + this.basePrice
				+ "\nminimumRise: " + this.minimumRise
				+ "\nexpireDateTime: " + this.expireDateTime
				+ "\n\nitemCodeError: " + this.itemCodeError
				+ "\nitemNameError: " + this.itemNameError
				+ "\nitemDescriptionError: " + this.itemDescriptionError
				+ "\nitemImageError: " + this.itemImageError
				+ "\n\nbasePriceError: " + this.basePriceError
				+ "\nminimumRiseError: " + this.minimumRiseError
				+ "\nexpireDateTimeError: " + this.expireDateTimeError
				+ "\n\nsucessMessage: " + this.successMessage;
	}
}
