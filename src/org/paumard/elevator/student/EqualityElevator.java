package org.paumard.elevator.student;

import org.paumard.elevator.Building;
import org.paumard.elevator.Elevator;
import org.paumard.elevator.event.DIRECTION;
import org.paumard.elevator.model.Person;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EqualityElevator implements Elevator {
	private static final int ANGER_LIMIT_THRESHOLD = 360;
	public static final LocalTime LEAVING_TIME = LocalTime.of(16, 30, 0);
	private DIRECTION direction = DIRECTION.UP;
	private int currentFloor = 1;
	private List<List<Person>> peopleByFloor = List.of();
	private List<Person> people = new ArrayList<>();
	private final int capacity;
	private LocalTime time;
	private List<Integer> destinations = new ArrayList<>();
	int nextDestination;
	private String id;
	DumbElevator e;

	private int peopleInFloor1;

	public EqualityElevator(int capacity, String id, DumbElevator e) {
		this.capacity = capacity;
		this.id = id;
		this.e = e;
	}

	@Override
	public void startsAtFloor(LocalTime time, int initialFloor) {
		this.time = time;
	}

	@Override
	public void peopleWaiting(List<List<Person>> peopleByFloor) {
		this.peopleByFloor = peopleByFloor;
	}

	private List<Integer> floorsWithAngryPeople() {
		int numberOfPeopleWaiting = countWaitingPeople();
		List<Integer> result = new ArrayList<Integer>();
		for (int indexFloor = 0; indexFloor < 10; indexFloor++) {
			List<Person> waitingList = this.peopleByFloor.get(indexFloor);
			if (!waitingList.isEmpty()) {
				Person mostPatientPerson = waitingList.get(0);
				LocalTime arrivalTime = mostPatientPerson.getArrivalTime();
				Duration waitingTime = Duration.between(arrivalTime, this.time);
				long waitingTimeInSeconds = waitingTime.toSeconds();
				if (waitingTimeInSeconds > ANGER_LIMIT_THRESHOLD) {
					result.add(indexFloor + 1);
				}
			}
		}
		//		System.out.println(this.time + "    "  + result + "    ");
		return result;
	}

	@Override
	public List<Integer> chooseNextFloors() {
		if (!this.destinations.isEmpty()) {

			if (this.time.isAfter(LEAVING_TIME)) 
				e.setOrderedToTakeDown(true);

				
		return this.destinations;
	}

	int numberOfPeopleWaiting = countWaitingPeople();
	if (numberOfPeopleWaiting > 0) {
		List<Integer> nonEmptyFloors = findNonEmptyFloor();
		int nonEmptyFloor = nonEmptyFloors.get(0);
		if (nonEmptyFloor != this.currentFloor) {
			return List.of(nonEmptyFloor);
		} else {

			int indexOfCurrentFloor = this.currentFloor - 1;
			List<Person> waitingListForCurrentFloor =
					this.peopleByFloor.get(indexOfCurrentFloor);

			List<Integer> destinationFloorsForCurrentFloor =
					findDestinationFloors(waitingListForCurrentFloor);
			destinations.addAll(destinationFloorsForCurrentFloor);

			for (Integer integer : weCanTakeThemOnTheWayUp()) {
				if (!destinations.contains(integer))
					destinations.add(integer);
			}

			if (this.time.isAfter(LEAVING_TIME))
				e.setOrderedToTakeDown(true);


			destinations.sort(Comparator.naturalOrder());
			return destinations;

		}}
	return List.of(1);
}

private List<Integer> weCanTakeThemOnTheWayUp(){

	List<Integer> intermediateFloors = new ArrayList<>();

	destinations.sort(Comparator.reverseOrder());

	for (int indexFloor = currentFloor; indexFloor < destinations.get(0) ; indexFloor ++) {

		List<Person> waitingList = this.peopleByFloor.get(indexFloor);

		if (!waitingList.isEmpty()) {

			for (Person p : waitingList) {

				if(p.getDestinationFloor() > getPersonsFloor(p)) {
					intermediateFloors.add(indexFloor+1);
					intermediateFloors.add(p.getDestinationFloor());
				}
			}
		}
	}
	return intermediateFloors;		
}

private List<Integer> findDestinationFloors(List<Person> waitingListForCurrentFloor) {
	return waitingListForCurrentFloor.stream()
			.map(person -> person.getDestinationFloor())
			.distinct()
			.collect(Collectors.toList());
}

private int getPersonsFloor(Person person) {

	List<Person> list = peopleByFloor.stream()
			.filter(p -> p.contains(person))
			.findAny()
			.get();

	return peopleByFloor.indexOf(list);
}

private List<Integer> findNonEmptyFloor() {
	for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
		if (!peopleByFloor.get(indexFloor).isEmpty()) {
			return List.of(indexFloor + 1);
		}
	}
	return List.of(-1);
}

private int countWaitingPeople() {
	return peopleByFloor.stream()
			.mapToInt(list -> list.size())
			.sum();
}

@Override
public void arriveAtFloor(int floor) {
	if (!this.destinations.isEmpty()) {
		this.destinations.remove(0);
	}

	this.currentFloor = floor;
}


@Override
public void loadPeople(List<Person> people) {
	this.people.addAll(people);  
	int indexFloor = this.currentFloor -1;
	orderToTakePeopleDown();
	//		this.peopleByFloor.get(indexFloor).removeAll(people);
	//		if(peopleByFloor.get(0).size() >= 10) {
	//			List<List<Person>> peopleByFloor2 = peopleByFloor;
	//			for (int indexFlr = 1 ; indexFlr < Building.MAX_FLOOR ; indexFlr++) {
	//				peopleByFloor2.get(indexFlr).clear();
	//			}
	//			this.e.setPeopleByFloor(peopleByFloor2);
	//		}
	//		else
	//		this.e.setPeopleByFloor(this.peopleByFloor);

}

@Override
public void unload(List<Person> person) {
	this.people.removeAll(people);
}

public void orderToTakePeopleDown() {

	List<Integer> destinationsToPickUpAngryPeople = floorsWithAngryPeople();
	//		System.out.println(this.time +"  " + destinationsToPickUpAngryPeople.size());

	if (destinationsToPickUpAngryPeople.size() > 5) 
		this.e.setOrderedToTakeDown(true);
	else 
		this.e.setOrderedToTakeDown(false);

}

@Override
public void newPersonWaitingAtFloor(int floor, Person person) {
	int indexFloor = floor - 1;
	
	if (person.getDestinationFloor() != floor)
	this.peopleByFloor.get(indexFloor).add(person);
	orderToTakePeopleDown();
	//		if(peopleByFloor.get(0).size() >= 10) {
	//			List<List<Person>> peopleByFloor2 = peopleByFloor;
	//			for (int indexFlr = 1 ; indexFlr < Building.MAX_FLOOR ; indexFlr++) {
	//				peopleByFloor2.get(indexFlr).clear();
	//			}
	//			this.e.setPeopleByFloor(peopleByFloor2);
	//		}
	//		else
	//		this.e.setPeopleByFloor(this.peopleByFloor);
}

@Override
public void lastPersonArrived() {
}

@Override
public void timeIs(LocalTime time) {
	this.time = time;
}

@Override
public void standByAtFloor(int currentFloor) {
}

@Override
public String getId() {
	// TODO Auto-generated method stub
	return this.id;
}
}




