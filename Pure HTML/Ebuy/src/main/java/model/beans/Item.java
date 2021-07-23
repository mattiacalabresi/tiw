package model.beans;

import org.eclipse.jdt.annotation.NonNull;

import controller.utils.Values;

/**
 * This class represents an Item to be
 * sold or bought through an Auction.
 */
public class Item {
	private static final int NULL_ID = -1;
	
	/**
	 * The identifier of the Item.
	 */
	private int id;
	/**
	 * The code of the Item.
	 */
	@NonNull
	private final String code;
	/**
	 * The name of the Item.
	 */
	@NonNull
	private final String name;
	/**
	 * The description of the Item.
	 */
	@NonNull
	private final String description;
	/**
	 * The name of the image representing
	 * the Item.
	 */
	@NonNull
	private final String imageName;

	public Item(final int id, @NonNull final String code, @NonNull final String name, @NonNull final String description, @NonNull final String imageName) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.imageName = imageName;
	}
	
	public Item(@NonNull final String code, @NonNull final String name, @NonNull final String description, @NonNull final String imagePath) {
		this(NULL_ID, code, name, description, imagePath);
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public int getId() {
		if(this.id == NULL_ID)
			throw new NullPointerException("Attempt to retrieve a null ID");
		return this.id;
	}

	@NonNull
	public String getCode() {
		return this.code;
	}

	@NonNull
	public String getName() {
		return this.name;
	}

	@NonNull
	public String getDescription() {
		return this.description;
	}

	@NonNull
	public String getImageName() {
		return this.imageName;
	}
	
	@NonNull
	public String getImagePath() {
		return Values.Params.IMAGE_FOLDER_PATH + this.imageName;
	}
}
