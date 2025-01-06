package org.mafutsu.util;

import java.awt.Color;

public class Constants {
  public static final String POSITION_TOP = "TOP";
  public static final String POSITION_JUNGLE = "JUNGLE";
  public static final String POSITION_MIDDLE = "MIDDLE";
  public static final String POSITION_BOTTOM = "BOTTOM";
  public static final String POSITION_UTILITY = "UTILITY";
  public static final String POSITION_FILL = "FILL";
  public static final String POSITION_UNSELECTED = "UNSELECTED";
  public static final String[] ROLES_STR = new String[] {POSITION_TOP, POSITION_JUNGLE, POSITION_MIDDLE, POSITION_BOTTOM, POSITION_UTILITY, POSITION_FILL};

  public static final String READYCHECK_NONE = "None";
  public static final String READYCHECK_ACCEPTED = "Accepted";
  public static final String READYCHECK_DECLINED = "Declined";

  public static final String SEARCH_INVALID = "Invalid";
  public static final String SEARCH_ABANDONED = "AbandonedLowPriorityQueue";
  public static final String SEARCH_CANCELD = "Canceled";
  public static final String SEARCH_SEARCHING = "Searching";
  public static final String SEARCH_FOUND = "Found";
  public static final String SEARCH_ERROR = "Error";
  public static final String SEARCH_SERVICE_ERROR = "ServiceError";
  public static final String SEARCH_SERVICE_SHUTDOWN = "ServiceShutdown";

  public static final String GET = "GET";
  public static final String POST = "POST";
  public static final String PUT = "PUT";
  public static final String PATCH = "PATCH";
  public static final String DELETE = "DELETE";

  public static final Color COLOR_RED = new Color(156, 69, 58);
  public static final Color COLOR_RED_10 = new Color(86, 79, 80);
  public static final Color COLOR_GREEN = new Color(58, 156, 84);
  public static final Color COLOR_GREEN_10 = new Color(76, 88, 82);
}
