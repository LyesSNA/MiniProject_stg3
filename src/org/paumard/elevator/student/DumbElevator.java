package org.paumard.elevator.student;

import org.paumard.elevator.Building;
import org.paumard.elevator.Elevator;
import org.paumard.elevator.model.Person;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DumbElevator implements Elevator {
	public static final LocalTime LEAVING_TIME = LocalTime.of(16, 30, 0);
	private static final int ANGER_LIMIT_THRESHOLD = 180;
	private int currentFloor = 1;
	private List<List<Person>> peopleByFloor = List.of();

	private List<Person> people = new ArrayList<>();
	private final int capacity;
	private LocalTime time;
	private List<Integer> destinations = List.of();

	private final String id;

	private boolean orderedToTakeDown;


	public DumbElevator(int capacity, String id) {
		this.id = id;
		this.capacity = capacity;
	}

	@Override
	public void startsAtFloor(LocalTime time, int initialFloor) {
		this.time = time;
		this.currentFloor = initialFloor;
	}

	@Override
	public void peopleWaiting(List<List<Person>> peopleByFloor) {
		this.peopleByFloor = peopleByFloor;
	}

	@Override
	public List<Integer> chooseNextFloors() {

		if (!this.destinations.isEmpty()) {
		}

		if (orderedToTakeDown == true) {
			if (!destinations.contains(10))
			destinations.add(10);
			if(currentFloor == 10) {
				destinations.addAll(List.of(9, 8, 7, 6, 5, 4, 3, 2, 1));
//				
//				for (Integer integer : destinations) {
//					if (peopleByFloor.get(integer-1).isEmpty())
//						destinations.remove(integer);
//				}
//				
				destinations.remove(0);
//				System.out.println(this.time +  "      here     " + destinations);
				return destinations;
			}
			return this.destinations;
		}

		int numberOfPeopleWaiting = countWaitingPeople();
		if (numberOfPeopleWaiting > 0 && this.time != Building.END_OF_DAY) {
			List<Integer> destinationsbyFloors = PickupPeopleFromFloors();
			int nonEmptyFloor = findNonEmptyFloor().get(0);
			if (nonEmptyFloor != this.currentFloor && destinationsbyFloors.isEmpty()) {
				return List.of(nonEmptyFloor);
			}

			if (!destinationsbyFloors.isEmpty()) {
				this.destinations = destinationsbyFloors;
				return this.destinations;
			} 
		}

		return List.of(1);

	}

	private List<Integer> PickupPeopleFromFloors() {
		int indexOfCurrentFloor = this.currentFloor - 1;
		List<Person> waitingListForCurrentFloor = this.peopleByFloor.get(indexOfCurrentFloor);

		List<Integer> destinationFloorsForCurrentFloor = findDestinationFloors(waitingListForCurrentFloor);
		this.destinations = destinationFloorsForCurrentFloor;
		return this.destinations;
	}

	private List<Integer> findDestinationFloors(List<Person> waitingListForCurrentFloor) {

		return waitingListForCurrentFloor.stream().map(person -> person.getDestinationFloor()).distinct().sorted()
				.collect(Collectors.toList());

	}

	private List<Integer> findNonEmptyFloor() {
		Map<Integer, Integer> floorsanddestinations = new HashMap<>();
		for (int indexFloor = 0; indexFloor < Building.MAX_FLOOR; indexFloor++) {
			if (!peopleByFloor.get(indexFloor).isEmpty()) {
				floorsanddestinations.put(indexFloor + 1, peopleByFloor.get(indexFloor).size());
				floorsanddestinations.entrySet().stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed());
				List<Integer> sorteddestinationsbyFloors = floorsanddestinations.entrySet().stream()
						.map(p -> p.getKey()).collect(Collectors.toList());
				return sorteddestinationsbyFloors;
			}

		}
		return List.of(-1);
	}

	private int countWaitingPeople() {
		return peopleByFloor.stream().mapToInt(list -> list.size()).sum();
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
		int indexFloor = this.currentFloor - 1;
		this.peopleByFloor.get(indexFloor).removeAll(people);
	}

	@Override
	public void unload(List<Person> people) {
		this.people.removeAll(people);
	}

	@Override
	public void newPersonWaitingAtFloor(int floor, Person person) {
		int indexFloor = floor - 1;
		this.peopleByFloor.get(indexFloor).add(person);
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
		return this.id;
	}

	public int getCapacity() {
		return capacity;
	}
	public void setDestinations(List<Integer> destinations) {
		this.destinations = destinations;
	}
	public void setPeopleByFloor(List<List<Person>> peopleByFloor) {
		this.peopleByFloor = peopleByFloor;
	}
	public void setOrderedToTakeDown(boolean orderToTakeDown) {
		this.orderedToTakeDown = orderToTakeDown;
	}
}