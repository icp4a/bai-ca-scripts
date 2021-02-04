/*
* Copyright IBM Corp. 1987, 2018
* 
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
* 
**/

package loan;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Borrower implements java.io.Serializable {

	private static final long serialVersionUID = 888265255939699136L;
	private String   firstName;
	private String   lastName;
	private Calendar birth;
	private SSN      SSN;
	private int      yearlyIncome;
	private String   zipCode ;
	private int      creditScore;
	private Borrower spouse;
	
	public class SSN implements java.io.Serializable {

		private static final long serialVersionUID = -2186494815176523547L;
		private String areaNumber; 
		private String groupCode;
		private String serialNumber;
		
		private void parseSSN(String number) {
			int firstDash = number.indexOf('-');
			if (firstDash >= 1) {
				areaNumber = number.substring(0, firstDash);
				int secondDash = number.indexOf('-', firstDash+1);
				if (secondDash >= firstDash+2) {
					groupCode = number.substring(firstDash+1, secondDash);
					serialNumber = number.substring(secondDash+1);
				} 
				else {
					groupCode = number.substring(firstDash+1, Math.min(number.length(), firstDash+3));
					serialNumber = number.substring(Math.min(number.length(), firstDash+3), number.length());
				}
			}
			else {
				areaNumber = number.substring(0, Math.min(number.length(), 3));
				groupCode = number.substring(Math.min(number.length(), 3), Math.min(number.length(), 5));
				serialNumber = number.substring(Math.min(number.length(), 5), number.length());
			}
		}

		@SuppressWarnings("unused")
		private SSN() {
		}

		public SSN(String number) {
			parseSSN(number);
		}
		
		public SSN(String areaNumber, String groupCode, String serialNumber) {
			this.areaNumber = areaNumber;
			this.groupCode = groupCode;
			this.serialNumber = serialNumber;
		}
		
		@JsonIgnore public int getDigits() {
			return areaNumber.length() + groupCode.length() + serialNumber.length();
		}

		public String getAreaNumber() {
			return areaNumber;
		}
		
		public void setAreaNumber(String areaNumber) {
			this.areaNumber = areaNumber;
		}
		
		public String getGroupCode() {
			return groupCode;
		}
		
		public void setGroupCode(String groupCode) {
			this.groupCode = groupCode;
		}
		
		public String getSerialNumber() {
			return serialNumber;
		}
		
		public void setSerialNumber(String serialNumber) {
			this.serialNumber = serialNumber;
		}
		
		@JsonIgnore public String getFullNumber() {
			return getAreaNumber() + "-" + getGroupCode() + "-" + getSerialNumber();
		}
		
		@JsonIgnore public String toString() {
			return this.getFullNumber();
		}
	}
	


	public class Bankruptcy implements java.io.Serializable {

		private static final long serialVersionUID = 3107842066700686170L;
		private Date date;
		private int chapter;
		private String reason;

		@SuppressWarnings("unused")
		private Bankruptcy() {
		}

		public Bankruptcy(Date date, int chapter, String reason) {
			this.date = date;
			this.chapter = chapter;
			this.reason = reason;
		}

		public Date getDate() {
			return date;
		}
		
		public void setDate(Date date) {
			this.date = date;
		}

		public String getReason() {
			return reason;
		}
		
		public void setReason(String reason) {
			this.reason = reason;
		}
		
		public int getChapter() {
			return this.chapter;
		}
		
		public void setChapter(int chapter) {
			this.chapter = chapter;
		}
	}

	/**
	 * @return Returns the creditScore.
	 */
	public int getCreditScore() {
		return creditScore;
	}
	/**
	 * @param creditScore The creditScore to set.
	 */
	public void setCreditScore(int creditScore) {
		this.creditScore = creditScore;
	}
	
	public Bankruptcy latestBankruptcy;

	public Borrower() {
		//Default date
		Date birthDate = new Date();
		Calendar cal = Calendar.getInstance();
		birthDate = DateUtil.dateAsDay(birthDate);
		cal.setTime(birthDate);
		this.birth = cal;
		
		//Default SSN
		this.SSN = new SSN("408-414-10000");
	}
	
	public Borrower(String firstName, String lastName, 
			Date birthDate,	String SSNCode) {
		this.firstName = firstName;
		this.lastName = lastName;
		Calendar cal = Calendar.getInstance();
		birthDate = DateUtil.dateAsDay(birthDate);
		cal.setTime(birthDate);
		this.birth = cal;
		this.SSN = new SSN(SSNCode);
	}
	
	public String toString() {
		String msg = Messages.getMessage("borrower");
		Object[] arguments = { firstName, lastName,
				DateUtil.format(getBirthDate()), getSSN() };
		String result = MessageFormat.format(msg, arguments);
	     
	     if (zipCode != null) {
			Object[] zipCodeObj = { getZipCode() };
			String zipCodeStr = MessageFormat.format(Messages
					.getMessage("zipCode"), zipCodeObj);
			result = result + "\n" + "   - " + zipCodeStr;
	     }
	     
	     if (yearlyIncome != 0) {
			Object[] incomeObj = { getYearlyIncome() };
			String incomeStr = MessageFormat.format(Messages
					.getMessage("yearlyIncome"), incomeObj);
			result = result + "\n" + "   - " + incomeStr;
	     }
	     
	     if (creditScore>0) {
			Object[] creditScoreObj = { getCreditScore() };
			String creditScoreStr = MessageFormat.format(Messages
					.getMessage("creditScore"), creditScoreObj);
			result = result + "\n" + "   - " + creditScoreStr;
	     }
	     
	     if (hasLatestBankrupcy()) {
			Object[] bankruptcyObj = {
					DateUtil.format(getLatestBankruptcyDate()),
					getLatestBankruptcyReason(), getLatestBankruptcyChapter() };
			String bankruptcyStr = MessageFormat.format(Messages
					.getMessage("bankruptcy"), bankruptcyObj);
			result = result + "\n" + "   - " + bankruptcyStr;
	     }
	     
		return result;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@JsonIgnore public Date getBirthDate() {
		return birth.getTime();
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public SSN getSSN() {
		return this.SSN;
	}
	
	public void setSSN(SSN theSSN) {
		this.SSN = theSSN;
	}

	@JsonIgnore public String getssncode() {
		return SSN.toString();
	}

	public int getYearlyIncome() {
		return yearlyIncome;
	}

	public void setYearlyIncome(int income) {
		this.yearlyIncome = income;
	}

	@JsonIgnore public boolean hasLatestBankrupcy() {
		return latestBankruptcy != null;
	}

	@JsonIgnore public Date getLatestBankruptcyDate() {
		return latestBankruptcy.getDate();
	}

	@JsonIgnore public String getLatestBankruptcyReason() {
		return latestBankruptcy.getReason();
	}
	// Among Unemployment; Large medical expenses; Seriously overextended credit; Marital problems, and Other large unexpected expenses

	@JsonIgnore public int getLatestBankruptcyChapter() {
		return latestBankruptcy.getChapter();
	}
	
	public void setLatestBankruptcy(Date date, int chapter, String reason) {
		this.latestBankruptcy = new Bankruptcy(date, chapter, reason);
	}
	
	public void setSpouse(Borrower spouse) {
	    this.spouse = spouse;
	}
	
	public Borrower getSpouse() {
	    return spouse;
	}

};
