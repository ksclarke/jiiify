package info.freelibrary.jiiify;
/**
 * BEGIN GENERATED CODE
 */
public interface MessageCodes {

	/**
	 * Message: Failed to add new folder '{}' to watch list
	 */
	public static final String EXC_030 = "EXC-030";
	/**
	 * Message: Watching '{}' for new images to ingest
	 */
	public static final String INFO_002 = "INFO-002";
	/**
	 * Message: Ingesting added or updated image file: {}
	 */
	public static final String INFO_001 = "INFO-001";
	/**
	 * Message: Failed to add new image file '{}' to watch list
	 */
	public static final String EXC_029 = "EXC-029";
	/**
	 * Message: Unable to send image path '{}' to tile master [{} attempts]: {}
	 */
	public static final String EXC_027 = "EXC-027";
	/**
	 * Message: No supported image reader available for {}
	 */
	public static final String EXC_026 = "EXC-026";
	/**
	 * Message: The ingest watch folder is not configured
	 */
	public static final String EXC_025 = "EXC-025";
	/**
	 * Message: Cannot create the ingest watch folder: {}
	 */
	public static final String EXC_024 = "EXC-024";
	/**
	 * Message: test message: {} and {}
	 */
	public static final String TEST_002 = "TEST-002";
	/**
	 * Message: Cannot write to the ingest watch folder: {}
	 */
	public static final String EXC_023 = "EXC-023";
	/**
	 * Message: test message
	 */
	public static final String TEST_001 = "TEST-001";
	/**
	 * Message: Cannot create the file uploads directory: {}
	 */
	public static final String EXC_022 = "EXC-022";
	/**
	 * Message: Cannot write to the file uploads directory: {}
	 */
	public static final String EXC_021 = "EXC-021";
	/**
	 * Message: Supplied region did not have four coordinates: {}
	 */
	public static final String EXC_020 = "EXC-020";
	/**
	 * Message: Server failed to start at port '{}'
	 */
	public static final String DBG_007 = "DBG-007";
	/**
	 * Message: Successfully deployed {} [{}]
	 */
	public static final String DBG_006 = "DBG-006";
	/**
	 * Message: Checking for new watch folder events
	 */
	public static final String DBG_005 = "DBG-005";
	/**
	 * Message: New file has been added to watched folder: {}
	 */
	public static final String DBG_004 = "DBG-004";
	/**
	 * Message: Unable to reset WatchKey for {} so removing it from the watch list
	 */
	public static final String DBG_003 = "DBG-003";
	/**
	 * Message: No more batch ingest folders are being watched
	 */
	public static final String DBG_002 = "DBG-002";
	/**
	 * Message: New ingest folder added to the watch list: {}
	 */
	public static final String DBG_001 = "DBG-001";
	/**
	 * Message: Successfully undeployed {} [{}]
	 */
	public static final String DBG_000 = "DBG-000";
	/**
	 * Message: Unexpected Region enumeration value: {}
	 */
	public static final String EXC_019 = "EXC-019";
	/**
	 * Message: Supplied region coordinate is not a float: {}
	 */
	public static final String EXC_018 = "EXC-018";
	/**
	 * Message: Supplied size {} is not an integer: {}
	 */
	public static final String EXC_017 = "EXC-017";
	/**
	 * Message: Supplied size has more than one comma: {}
	 */
	public static final String EXC_016 = "EXC-016";
	/**
	 * Message: Supplied size only contained a comma, no height or width
	 */
	public static final String EXC_015 = "EXC-015";
	/**
	 * Message: Supplied size '{}' is not an integer between 1 and 100
	 */
	public static final String EXC_014 = "EXC-014";
	/**
	 * Message: Supplied rotation '{}' is not a float in the range of 0 to 360
	 */
	public static final String EXC_013 = "EXC-013";
	/**
	 * Message: Supplied quality '{}' is not one of the supported qualities: {}
	 */
	public static final String EXC_012 = "EXC-012";
	/**
	 * Message: Supplied format '{}' is not one of the supported formats: {}
	 */
	public static final String EXC_011 = "EXC-011";
	/**
	 * Message: Failed to send image path '{}' so trying again
	 */
	public static final String WARN_002 = "WARN-002";
	/**
	 * Message: Failed to send tile path ({}) so trying again
	 */
	public static final String WARN_001 = "WARN-001";
	/**
	 * Message: Cannot create the data directory: {}
	 */
	public static final String EXC_036 = "EXC-036";
	/**
	 * Message: Cannot write to the data directory: {}
	 */
	public static final String EXC_035 = "EXC-035";
	/**
	 * Message: Unable to send tile path ({}) to image worker [{} attempts]: {}
	 */
	public static final String EXC_034 = "EXC-034";
	/**
	 * Message: Tile size from {} wasn't valid so using the default '{}' instead
	 */
	public static final String EXC_033 = "EXC-033";
	/**
	 * Message: Trying to calculate tiles, but image file '{}' does not exist
	 */
	public static final String EXC_032 = "EXC-032";
	/**
	 * Message: {}
	 */
	public static final String EXC_000 = "EXC-000";
	/**
	 * Message: The polled WatchKey does not correspond to a watched directory
	 */
	public static final String EXC_031 = "EXC-031";
}