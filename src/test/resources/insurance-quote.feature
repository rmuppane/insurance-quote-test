Feature: Test case 'insurance-quote-process'

    Scenario: ManualDecision
    		Given a customer approached for insurance
    		When a customer approached for insurance with the following data
        # DataEntry - Start
      	And the human task 'DataEntry' is claimed by 'rhpamAdmin'
      	And the human task 'DataEntry' is 'Started' by 'rhpamAdmin'
      	And the human task 'DataEntry' is 'Completed' by 'rhpamAdmin' with parameters
          	|firstName   |lastName   |DOB   			|faceAmount   |income 		 |
          	|John				 |Finn		   |11/11/2011  |155000		    |50000 		   |
        Then the decision from decision chart is
        		|chartDecision  |
            |Manual    |
       	And the human task 'UW Decision' is claimed by 'rhpamAdmin'
        And the human task 'UW Decision' is 'Started' by 'rhpamAdmin'
        And the human task 'UW Decision' is 'Completed' by 'rhpamAdmin' with parameters
        		|uwDecision  |
            |Approved    |
            
    Scenario: Auto Approvval
    		When a customer approached for insurance with the following data
        # DataEntry - Start
	        And the human task 'DataEntry' is claimed by 'rhpamAdmin'
	        And the human task 'DataEntry' is 'Started' by 'rhpamAdmin'
	        And the human task 'DataEntry' is 'Completed' by 'rhpamAdmin' with parameters
	            |firstName   |lastName   |DOB   			|faceAmount   |income 		 |
	            |John				 |Finn		   |11/11/2011  |155000		    |50000 		   |
        Then the decision from decision chart is
        		|chartDecision  |
            |Approved       |
	       	And the human task 'Prepare Policy Documentation' is claimed by 'rhpamAdmin'
	        And the human task 'Prepare Policy Documentation' is 'Started' by 'rhpamAdmin'
	        And the human task 'Prepare Policy Documentation' is 'Completed' by 'rhpamAdmin' with parameters
	        		|document  |
	            |true      |
	            
    Scenario: Auto Declined
    		When a customer approached for insurance with the following data
        # DataEntry - Start
	        And the human task 'DataEntry' is claimed by 'rhpamAdmin'
	        And the human task 'DataEntry' is 'Started' by 'rhpamAdmin'
	        And the human task 'DataEntry' is 'Completed' by 'rhpamAdmin' with parameters
	            |firstName   |lastName   |DOB   			|faceAmount   |income 		 |
	            |John				 |Finn		   |11/11/2011  |540000		    |50000 		   |
        Then the decision from decision chart is
        		|chartDecision  |
            |Declined       |
	       	And the human task 'Documentation' is claimed by 'rhpamAdmin'
	        And the human task 'Documentation' is 'Started' by 'rhpamAdmin'
	        And the human task 'Documentation' is 'Completed' by 'rhpamAdmin' with parameters
	        		|document  |
	            |true      |
	            
	            
	  Scenario: Auto Declined Negative faceamount
    		When a customer approached for insurance with the following data
        # DataEntry - Start
	        And the human task 'DataEntry' is claimed by 'rhpamAdmin'
	        And the human task 'DataEntry' is 'Started' by 'rhpamAdmin'
	        And the human task 'DataEntry' is 'Completed' by 'rhpamAdmin' with parameters
	            |firstName   |lastName   |DOB   			|faceAmount   |income 		 |
	            |John				 |Finn		   |11/11/2011  |-50000		    |50000 		   |
        Then the decision from decision chart is
        		|chartDecision  |
            |Declined       |
	       	And the human task 'Documentation' is claimed by 'rhpamAdmin'
	        And the human task 'Documentation' is 'Started' by 'rhpamAdmin'
	        And the human task 'Documentation' is 'Completed' by 'rhpamAdmin' with parameters
	        		|document  |
	            |true      |
	            
	            