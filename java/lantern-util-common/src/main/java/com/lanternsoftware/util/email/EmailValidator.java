package com.lanternsoftware.util.email;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
	private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
	private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";
	private static final String QUOTED_USER = "(\"[^\"]*\")";
	private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

	private static final String LEGAL_ASCII_REGEX = "^\\p{ASCII}+$";
	private static final String EMAIL_REGEX = "^\\s*?(.+)@(.+?)\\s*$";
	private static final String IP_DOMAIN_REGEX = "^\\[(.*)\\]$";
	private static final String USER_REGEX = "^\\s*" + WORD + "(\\." + WORD + ")*$";

	private static final Pattern MATCH_ASCII_PATTERN = Pattern.compile(LEGAL_ASCII_REGEX);
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
	private static final Pattern IP_DOMAIN_PATTERN = Pattern.compile(IP_DOMAIN_REGEX);
	private static final Pattern USER_PATTERN = Pattern.compile(USER_REGEX);

	private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
	private final RegexValidator ipv4Validator = new RegexValidator(IPV4_REGEX);

	private static final String DOMAIN_LABEL_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";
	private static final String TOP_LABEL_REGEX = "\\p{Alpha}{2,}";
	private static final String DOMAIN_NAME_REGEX = "^(?:" + DOMAIN_LABEL_REGEX + "\\.)+" + "(" + TOP_LABEL_REGEX + ")$";

	/** RegexValidator for matching domains. */
	private final RegexValidator domainRegex = new RegexValidator(DOMAIN_NAME_REGEX);

	private static final String[] INFRASTRUCTURE_TLDS = new String[] { "arpa", "root" };

	private static final String[] GENERIC_TLDS = new String[] { "aero", // air transport industry
			"asia", // Pan-Asia/Asia Pacific
			"biz", // businesses
			"cat", // Catalan linguistic/cultural community
			"com", // commercial enterprises
			"coop", // cooperative associations
			"info", // informational sites
			"jobs", // Human Resource managers
			"mobi", // mobile products and services
			"museum", // museums, surprisingly enough
			"name", // individuals' sites
			"net", // internet support infrastructure/business
			"org", // noncommercial organizations
			"pro", // credentialed professionals and entities
			"tel", // contact data for businesses and individuals
			"travel", // entities in the travel industry
			"gov", // United States Government
			"edu", // accredited postsecondary US education entities
			"mil", // United States Military
			"int" // organizations established by international treaty
	};

	private static final String[] COUNTRY_CODE_TLDS = new String[] { "ac", // Ascension Island
			"ad", // Andorra
			"ae", // United Arab Emirates
			"af", // Afghanistan
			"ag", // Antigua and Barbuda
			"ai", // Anguilla
			"al", // Albania
			"am", // Armenia
			"an", // Netherlands Antilles
			"ao", // Angola
			"aq", // Antarctica
			"ar", // Argentina
			"as", // American Samoa
			"at", // Austria
			"au", // Australia (includes Ashmore and Cartier Islands and Coral Sea Islands)
			"aw", // Aruba
			"ax", // Åland
			"az", // Azerbaijan
			"ba", // Bosnia and Herzegovina
			"bb", // Barbados
			"bd", // Bangladesh
			"be", // Belgium
			"bf", // Burkina Faso
			"bg", // Bulgaria
			"bh", // Bahrain
			"bi", // Burundi
			"bj", // Benin
			"bm", // Bermuda
			"bn", // Brunei Darussalam
			"bo", // Bolivia
			"br", // Brazil
			"bs", // Bahamas
			"bt", // Bhutan
			"bv", // Bouvet Island
			"bw", // Botswana
			"by", // Belarus
			"bz", // Belize
			"ca", // Canada
			"cc", // Cocos (Keeling) Islands
			"cd", // Democratic Republic of the Congo (formerly Zaire)
			"cf", // Central African Republic
			"cg", // Republic of the Congo
			"ch", // Switzerland
			"ci", // Côte d'Ivoire
			"ck", // Cook Islands
			"cl", // Chile
			"cm", // Cameroon
			"cn", // China, mainland
			"co", // Colombia
			"cr", // Costa Rica
			"cu", // Cuba
			"cv", // Cape Verde
			"cx", // Christmas Island
			"cy", // Cyprus
			"cz", // Czech Republic
			"de", // Germany
			"dj", // Djibouti
			"dk", // Denmark
			"dm", // Dominica
			"do", // Dominican Republic
			"dz", // Algeria
			"ec", // Ecuador
			"ee", // Estonia
			"eg", // Egypt
			"er", // Eritrea
			"es", // Spain
			"et", // Ethiopia
			"eu", // European Union
			"fi", // Finland
			"fj", // Fiji
			"fk", // Falkland Islands
			"fm", // Federated States of Micronesia
			"fo", // Faroe Islands
			"fr", // France
			"ga", // Gabon
			"gb", // Great Britain (United Kingdom)
			"gd", // Grenada
			"ge", // Georgia
			"gf", // French Guiana
			"gg", // Guernsey
			"gh", // Ghana
			"gi", // Gibraltar
			"gl", // Greenland
			"gm", // The Gambia
			"gn", // Guinea
			"gp", // Guadeloupe
			"gq", // Equatorial Guinea
			"gr", // Greece
			"gs", // South Georgia and the South Sandwich Islands
			"gt", // Guatemala
			"gu", // Guam
			"gw", // Guinea-Bissau
			"gy", // Guyana
			"hk", // Hong Kong
			"hm", // Heard Island and McDonald Islands
			"hn", // Honduras
			"hr", // Croatia (Hrvatska)
			"ht", // Haiti
			"hu", // Hungary
			"id", // Indonesia
			"ie", // Ireland (Éire)
			"il", // Israel
			"im", // Isle of Man
			"in", // India
			"io", // British Indian Ocean Territory
			"iq", // Iraq
			"ir", // Iran
			"is", // Iceland
			"it", // Italy
			"je", // Jersey
			"jm", // Jamaica
			"jo", // Jordan
			"jp", // Japan
			"ke", // Kenya
			"kg", // Kyrgyzstan
			"kh", // Cambodia (Khmer)
			"ki", // Kiribati
			"km", // Comoros
			"kn", // Saint Kitts and Nevis
			"kp", // North Korea
			"kr", // South Korea
			"kw", // Kuwait
			"ky", // Cayman Islands
			"kz", // Kazakhstan
			"la", // Laos (currently being marketed as the official domain for Los Angeles)
			"lb", // Lebanon
			"lc", // Saint Lucia
			"li", // Liechtenstein
			"lk", // Sri Lanka
			"lr", // Liberia
			"ls", // Lesotho
			"lt", // Lithuania
			"lu", // Luxembourg
			"lv", // Latvia
			"ly", // Libya
			"ma", // Morocco
			"mc", // Monaco
			"md", // Moldova
			"me", // Montenegro
			"mg", // Madagascar
			"mh", // Marshall Islands
			"mk", // Republic of Macedonia
			"ml", // Mali
			"mm", // Myanmar
			"mn", // Mongolia
			"mo", // Macau
			"mp", // Northern Mariana Islands
			"mq", // Martinique
			"mr", // Mauritania
			"ms", // Montserrat
			"mt", // Malta
			"mu", // Mauritius
			"mv", // Maldives
			"mw", // Malawi
			"mx", // Mexico
			"my", // Malaysia
			"mz", // Mozambique
			"na", // Namibia
			"nc", // New Caledonia
			"ne", // Niger
			"nf", // Norfolk Island
			"ng", // Nigeria
			"ni", // Nicaragua
			"nl", // Netherlands
			"no", // Norway
			"np", // Nepal
			"nr", // Nauru
			"nu", // Niue
			"nz", // New Zealand
			"om", // Oman
			"pa", // Panama
			"pe", // Peru
			"pf", // French Polynesia With Clipperton Island
			"pg", // Papua New Guinea
			"ph", // Philippines
			"pk", // Pakistan
			"pl", // Poland
			"pm", // Saint-Pierre and Miquelon
			"pn", // Pitcairn Islands
			"pr", // Puerto Rico
			"ps", // Palestinian territories (PA-controlled West Bank and Gaza Strip)
			"pt", // Portugal
			"pw", // Palau
			"py", // Paraguay
			"qa", // Qatar
			"re", // Réunion
			"ro", // Romania
			"rs", // Serbia
			"ru", // Russia
			"rw", // Rwanda
			"sa", // Saudi Arabia
			"sb", // Solomon Islands
			"sc", // Seychelles
			"sd", // Sudan
			"se", // Sweden
			"sg", // Singapore
			"sh", // Saint Helena
			"si", // Slovenia
			"sj", // Svalbard and Jan Mayen Islands Not in use (Norwegian dependencies; see .no)
			"sk", // Slovakia
			"sl", // Sierra Leone
			"sm", // San Marino
			"sn", // Senegal
			"so", // Somalia
			"sr", // Suriname
			"st", // São Tomé and Príncipe
			"su", // Soviet Union (deprecated)
			"sv", // El Salvador
			"sy", // Syria
			"sz", // Swaziland
			"tc", // Turks and Caicos Islands
			"td", // Chad
			"tf", // French Southern and Antarctic Lands
			"tg", // Togo
			"th", // Thailand
			"tj", // Tajikistan
			"tk", // Tokelau
			"tl", // East Timor (deprecated old code)
			"tm", // Turkmenistan
			"tn", // Tunisia
			"to", // Tonga
			"tp", // East Timor
			"tr", // Turkey
			"tt", // Trinidad and Tobago
			"tv", // Tuvalu
			"tw", // Taiwan, Republic of China
			"tz", // Tanzania
			"ua", // Ukraine
			"ug", // Uganda
			"uk", // United Kingdom
			"um", // United States Minor Outlying Islands
			"us", // United States of America
			"uy", // Uruguay
			"uz", // Uzbekistan
			"va", // Vatican City State
			"vc", // Saint Vincent and the Grenadines
			"ve", // Venezuela
			"vg", // British Virgin Islands
			"vi", // U.S. Virgin Islands
			"vn", // Vietnam
			"vu", // Vanuatu
			"wf", // Wallis and Futuna
			"ws", // Samoa (formerly Western Samoa)
			"ye", // Yemen
			"yt", // Mayotte
			"yu", // Serbia and Montenegro (originally Yugoslavia)
			"za", // South Africa
			"zm", // Zambia
			"zw", // Zimbabwe
	};

	private static final List<String> INFRASTRUCTURE_TLD_LIST = Arrays.asList(INFRASTRUCTURE_TLDS);
	private static final List<String> GENERIC_TLD_LIST = Arrays.asList(GENERIC_TLDS);
	private static final Set<String> COUNTRY_CODE_TLD_LIST = new HashSet<String>(Arrays.asList(COUNTRY_CODE_TLDS));

	private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

	public static EmailValidator getInstance()
	{
		return EMAIL_VALIDATOR;
	}

	public boolean isValid(String email)
	{
		if (email == null)
			return false;

		Matcher asciiMatcher = MATCH_ASCII_PATTERN.matcher(email);
		if (!asciiMatcher.matches())
			return false;

		// Check the whole email address structure
		Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
		if (!emailMatcher.matches())
			return false;

		if (email.endsWith("."))
			return false;

		if (!isValidUser(emailMatcher.group(1)))
			return false;

		if (!isValidDomain(emailMatcher.group(2)))
			return false;
		return true;
	}

	protected boolean isValidDomain(String domain)
	{
		// see if domain is an IP address in brackets
		Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

		if (ipDomainMatcher.matches())
			return isValidInet4Address(ipDomainMatcher.group(1));
		else
			return isValidTldDomain(domain);
	}

	protected boolean isValidInet4Address(String inet4Address)
	{
		// verify that address conforms to generic IPv4 format
		String[] groups = ipv4Validator.match(inet4Address);

		if (groups == null)
			return false;

		// verify that address subgroups are legal
		for (int i = 0; i <= 3; i++)
		{
			String ipSegment = groups[i];
			if (ipSegment == null || ipSegment.length() <= 0)
				return false;

			int iIpSegment = 0;

			try
			{
				iIpSegment = Integer.parseInt(ipSegment);
			}
			catch (NumberFormatException e)
			{
				return false;
			}
			if (iIpSegment > 255)
				return false;
		}
		return true;
	}

	protected boolean isValidUser(String user)
	{
		return USER_PATTERN.matcher(user).matches();
	}

	protected boolean isValidTldDomain(String domain)
	{
		String[] groups = domainRegex.match(domain);
		if (groups != null && groups.length > 0)
			return isValidTld(groups[0]);
		return false;
	}

	protected boolean isValidTld(String tld)
	{
		return isValidInfrastructureTld(tld) || isValidGenericTld(tld) || isValidCountryCodeTld(tld);
	}

	protected boolean isValidInfrastructureTld(String iTld)
	{
		return INFRASTRUCTURE_TLD_LIST.contains(chompLeadingDot(iTld.toLowerCase()));
	}

	protected boolean isValidGenericTld(String gTld)
	{
		return GENERIC_TLD_LIST.contains(chompLeadingDot(gTld.toLowerCase()));
	}

	protected boolean isValidCountryCodeTld(String ccTld)
	{
		return COUNTRY_CODE_TLD_LIST.contains(chompLeadingDot(ccTld.toLowerCase()));
	}

	private String chompLeadingDot(String str)
	{
		if (str.startsWith("."))
			return str.substring(1);
		else
			return str;
	}

	private class RegexValidator implements Serializable
	{
		private static final long serialVersionUID = -8832409930574867162L;

		private final Pattern[] patterns;

		/** Construct a <i>case sensitive</i> validator for a single regular expression.
		 *
		 * @param regex
		 *            The regular expression this validator will validate against */
		public RegexValidator(String regex)
		{
			this(regex, true);
		}

		/** Construct a validator for a single regular expression with the specified case sensitivity.
		 *
		 * @param regex
		 *            The regular expression this validator will validate against
		 * @param caseSensitive
		 *            when <code>true</code> matching is <i>case sensitive</i>, otherwise matching is <i>case in-sensitive</i> */
		public RegexValidator(String regex, boolean caseSensitive)
		{
			this(new String[] { regex }, caseSensitive);
		}

		/** Construct a validator that matches any one of the set of regular expressions with the specified case sensitivity.
		 *
		 * @param regexs
		 *            The set of regular expressions this validator will validate against
		 * @param caseSensitive
		 *            when <code>true</code> matching is <i>case sensitive</i>, otherwise matching is <i>case in-sensitive</i> */
		public RegexValidator(String[] regexs, boolean caseSensitive)
		{
			if (regexs == null || regexs.length == 0)
			{
				throw new IllegalArgumentException("Regular expressions are missing");
			}
			patterns = new Pattern[regexs.length];
			int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
			for (int i = 0; i < regexs.length; i++)
			{
				if (regexs[i] == null || regexs[i].length() == 0)
				{
					throw new IllegalArgumentException("Regular expression[" + i + "] is missing");
				}
				patterns[i] = Pattern.compile(regexs[i], flags);
			}
		}

		/** Validate a value against the set of regular expressions returning the array of matched groups.
		 *
		 * @param value
		 *            The value to validate.
		 * @return String array of the <i>groups</i> matched if valid or <code>null</code> if invalid */
		public String[] match(String value)
		{
			if (value == null)
			{
				return null;
			}
			for (int i = 0; i < patterns.length; i++)
			{
				Matcher matcher = patterns[i].matcher(value);
				if (matcher.matches())
				{
					int count = matcher.groupCount();
					String[] groups = new String[count];
					for (int j = 0; j < count; j++)
					{
						groups[j] = matcher.group(j + 1);
					}
					return groups;
				}
			}
			return null;
		}

		/** Provide a String representation of this validator.
		 *
		 * @return A String representation of this validator */
		public String toString()
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("RegexValidator{");
			for (int i = 0; i < patterns.length; i++)
			{
				if (i > 0)
				{
					buffer.append(",");
				}
				buffer.append(patterns[i].pattern());
			}
			buffer.append("}");
			return buffer.toString();
		}
	}
}
