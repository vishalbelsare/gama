/**
* Name: Save to CSV
* Author: Patrick Taillandier
* Description: This is a model that shows how to save agents inside a CSV File to reuse it later or to keep it.
* Tags : Save Files
*/
model SavetoCSV

global {
	init {
		//Create all the agents
		create bug number: 50;
	}
	//Save the agents bug when the cycle is equals to 100
	reflex save_bug_attribute when: cycle = 100{
		ask bug {
			// save the values of the variables name, speed and size to the csv file
			save [name,speed, size] to: "../results/bug.csv" type:"csv";
		}
		//Pause the model as the data are saved
		do pause;
	}
}

//Species that will be saved
species bug skills:[moving]{
	float size <- 1.0 + rnd(4) min: 1.0 max: 5.0;
	float speed <- 1.0 + rnd(4.0);
	
	reflex update_size {
		int nb_neigh <- length(bug at_distance 20.0);
		if (nb_neigh > 5) {
			size <- size + 1;
		} else {
			size <- size - 1;
		}
	} 	
	reflex move {
		do wander;
	}
	
	aspect default {
		draw circle(size) color: #red;
	}
}

experiment main type: gui {
	output {
		display map {
			species bug;
		}
	}
}
