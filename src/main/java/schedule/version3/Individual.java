package schedule.version3;


import schedule.version3.data.Classes;
import schedule.version3.data.Lesson;
import schedule.version3.data.TimeTable;

import java.util.*;


public class Individual {

    private List<Lesson>[][] timetable;
    private double fitness = -1;
    private int numOfClasses;
    private ArrayList<Integer>[] candidatesForMutation;  //int index is ClassID


    /**
     * Create individual from a timetable and auto-calculate other fields
     *
     * @param timetable The number of individuals in the population
     */

    public Individual(List<Lesson>[][] timetable) {
        this.timetable = timetable;
        this.numOfClasses = timetable.length;
        this.candidatesForMutation = new ArrayList[this.numOfClasses];
        for (int i = 0; i < this.numOfClasses; i++) {
            this.candidatesForMutation[i] = new ArrayList<Integer>();
        }
        this.fitness = calcFitness();
    }

    /**
     * Clone an individual
     *
     * @param individual The number of individuals in the population
     */
    public Individual(Individual individual) {
        this.timetable = individual.getTimetable();
        this.numOfClasses = individual.getNumOfClasses();
        this.candidatesForMutation = new ArrayList[this.numOfClasses];
        for (int i = 0; i < this.numOfClasses; i++) {
            this.candidatesForMutation[i] = new ArrayList<Integer>();
        }
        this.fitness = individual.calcFitness();
    }

    /**
     * Create an individual from scanned data for first generation
     *
     * @param allClasses ArrayList containing instances of all the classes
     */

    public Individual(List<Classes> allClasses) {
        this.timetable = TimeTable.createRandomTimeTable(allClasses);
        this.numOfClasses = allClasses.size();
        this.candidatesForMutation = new ArrayList[numOfClasses];
        for (int i = 0; i < numOfClasses; i++) {
            this.candidatesForMutation[i] = new ArrayList<Integer>();
        }
        this.fitness = calcFitness();
    }

    /**
     * Create an individual from scanned data for first generation
     *
     * @param parent1 Mother parent
     * @param parent2 Father parent
     */
    public static Individual breedOffspring(Individual parent1, Individual parent2, int[] allClassGrades) {
        List<Lesson>[][] offspringTimeTable = new ArrayList[allClassGrades.length][45];
        int classIndex = 0;
        int grade = allClassGrades[classIndex];
        Individual parent = chooseParent(parent1, parent2);

        while(classIndex < allClassGrades.length) {
            if(grade == allClassGrades[classIndex]) {
                offspringTimeTable[classIndex] = parent.getClassTimetable(classIndex);
            } else{
                parent = chooseParent(parent1, parent2);
                offspringTimeTable[classIndex] = parent.getClassTimetable(classIndex);
                grade = allClassGrades[classIndex];
            }
            classIndex++;
        }

        return new Individual(offspringTimeTable);
    }




        /*
        // Loop over genome for all the offspring candidates
        for (int classIndex = 0; classIndex < parent1.getNumOfClasses(); classIndex += 2) {
            // Use half of parent1's genes and half of parent2's genes
            if (0.5 > Math.random()) {
                offspringTimeTable[classIndex] = parent1.getClassTimetable(classIndex);
            } else {
                offspringTimeTable[classIndex] = parent2.getClassTimetable(classIndex);
            }
        }
        return new Individual(offspringTimeTable);
        }
        */


    private static Individual chooseParent(Individual parent1, Individual parent2) {
        if (0.5 > Math.random()) {
            return parent1;
        } else {
            return parent2;
        }
    }


    /**
     * Get number of classes
     */

    public int getNumOfClasses() {
        return numOfClasses;
    }

    /**
     * Clone individuals timetable
     */
    public List<Lesson>[][] getTimetable() {
        List<Lesson>[][] newTimeTable = new ArrayList[numOfClasses][45];
        for (int i = 0; i < numOfClasses; i++) {
            for (int j = 0; j < 45; j++) {
                newTimeTable[i][j] = new ArrayList<>(timetable[i][j].size());
                for (Lesson lesson : timetable[i][j]) {
                    newTimeTable[i][j].add(lesson);
                }
            }
        }
        return newTimeTable;
    }

    /**
     * Get individual's fitness value
     */
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Set individual's fitness value manually
     *
     * @param fitness Fitness value to set
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Calculate and set individual's fitness
     * Get candidates for mutation
     */
    public double calcFitness() {
        for (ArrayList<Integer> studentClass : candidatesForMutation) {
            studentClass.clear();
        }
        int clashes = 0;

        // clashes for same teacher in same timeslot

        Set<String> teachersForClasses = new HashSet<>();
        Set<String> teachersForGrades = new HashSet<>();
        Set<String> teachersForGradeGroups = new HashSet<>();

        for (int i = 0; i < timetable[0].length; i++) {
            for (int j = 0; j < timetable.length; j++) {
                for (int k = 0; k < timetable[j][i].size(); k++) {
                    String teacherName = timetable[j][i].get(k).getTeacher().getName();
                    String gradeID = timetable[j][i].get(k).getGroupID().substring(0, 3);

                    if (timetable[j][i].get(k).getValueOfFreeness() == 0) {
                        if (timetable[j][i].get(k).getGroupID().startsWith("1")) {
                            if (!teachersForClasses.contains(teacherName)) {
                                if (!teachersForGrades.contains(teacherName)) {
                                    teachersForGrades.add(teacherName);
                                    teachersForGradeGroups.add(teacherName + gradeID);
                                }
                                else if (!teachersForGradeGroups.contains(teacherName + gradeID)) {
                                    candidatesForMutation[j].add(i);
                                    clashes++;
                                }
                            } else {
                                candidatesForMutation[j].add(i);
                                clashes++;
                            }
                        } else if (!teachersForClasses.add(teacherName)
                                || teachersForGrades.contains(teacherName)) {
                            candidatesForMutation[j].add(i);
                            clashes++;
                        }
                    }
                }
            }
            teachersForClasses.clear();
            teachersForGrades.clear();
            teachersForGradeGroups.clear();
        }

        // clashes for same lesson in same day
        Set<String> set2 = new HashSet<>();
        for (int i = 0; i < timetable.length; i++) {
            for (int j = 0; j < timetable[i].length; j++) {
                if (j % 9 == 0) {
                    set2.clear();
                }
                for (int k = 0; k < timetable[i][j].size(); k++) {
                    if (timetable[i][j].get(k).getValueOfFreeness() == 0 && !set2.add(timetable[i][j].get(k).getNameOfLesson() + timetable[i][j].get(k).getGroupID())) {
                        candidatesForMutation[i].add(j);
                        clashes++;
                    }
                }
            }
        }

        // calculate fitness
        double calculatedFitness = (double) 1 / (double) (1 + clashes);
        // set fitness
        setFitness(calculatedFitness);
        return calculatedFitness;
    }

    /**
     * Mutate individual's timetable by swapping colliding classes
     *
     * @param classID Which classes' timetable to mutate
     */

    public void mutateTwoCollisions(int classID, int[] allClassGrades) {
        calcFitness();
        // how many collisions in a class timetable
        //System.out.println(candidatesForMutation[classID]);
        int collNum = candidatesForMutation[classID].size();
        if (collNum >= 2) {
            // choose two random indexes, and get Lesson day and hour location indexes from them
            int i = (int) Math.floor(Math.random() * collNum);
            int j;
            do {
                j = (int) Math.floor(Math.random() * collNum);
            } while (i == j);
            int dayHour1 = candidatesForMutation[classID].get(i);
            int dayHour2 = candidatesForMutation[classID].get(j);

            mutate(classID, dayHour1, dayHour2, allClassGrades);
        }
    }

    /**
     * Mutate individual's timetable by swapping one colliding classe
     * with a random class
     *
     * @param classID Which classes' timetable to mutate
     */
    public void mutateOneCollision(int classID, int[] allClassGrades) {
        calcFitness();
        // How many collisions in a class timetable
        int collNum = candidatesForMutation[classID].size();
        if (collNum >= 1) {
            // choose two random indexes, and get Lesson day and hour location indexes from them
            int i = (int) Math.floor(Math.random() * collNum);
            int dayHour1 = candidatesForMutation[classID].get(i);
            int dayHour2 = (int) Math.floor(Math.random() * timetable[classID].length);

            mutate(classID, dayHour1, dayHour2, allClassGrades);
        }
    }

    /**
     * Mutate individual's timetable by swapping random classes
     *
     * @param classID Which classes' timetable to mutate
     */
    public void mutateRandom(int classID, int[] allClassGrades) {
        // choose two random indexes, and get Lesson day and hour location indexes from them
        int dayHour1 = (int) Math.floor(Math.random() * timetable[classID].length);
        int dayHour2 = (int) Math.floor(Math.random() * timetable[classID].length);

        mutate(classID, dayHour1, dayHour2, allClassGrades);

    }


    /**
     * Clone a class' timetable
     *
     * @param classID Which classes' timetable to clone
     */
    public List<Lesson>[] getClassTimetable(int classID) {
        List<Lesson>[] classTimetable = new ArrayList[45];
        for (int i = 0; i < classTimetable.length; i++) {
            classTimetable[i] = new ArrayList<>(timetable[classID][i].size());
            for (Lesson lesson : timetable[classID][i]) {
                classTimetable[i].add(lesson);
            }
        }
        return classTimetable;
        //return this.timetable[classID];
    }

    private void swap(int classID, int dayHour1,int dayHour2) {
        List<Lesson> temp = new ArrayList<>(timetable[classID][dayHour2].size());
        for (Lesson lesson : timetable[classID][dayHour2]) {
            temp.add(lesson);
        }
        timetable[classID][dayHour2] = new ArrayList<>(timetable[classID][dayHour1].size());
        for (Lesson lesson : timetable[classID][dayHour1]) {
            timetable[classID][dayHour2].add(lesson);
        }
        timetable[classID][dayHour1] = temp;
    }

    private ArrayList<Integer> findOtherGroup(int classID, int[] allClassGrades) {
        int grade = allClassGrades[classID];
        ArrayList<Integer> others = new ArrayList<>();
        for (int i = 0; i < allClassGrades.length; i++) {
            if(i != classID && grade == allClassGrades[i]) {
                others.add(i);
            }
        }
        return others;
    }

    private void mutate(int classID, int dayHour1, int dayHour2, int[] allClassGrades) {
        if(timetable[classID][dayHour1].get(0).getGroupID().startsWith("1")
                && timetable[classID][dayHour2].get(0).getGroupID().startsWith("1")) {
            ArrayList<Integer> others = findOtherGroup(classID, allClassGrades);
            for (int id: others) {
                swap(id, dayHour1, dayHour2);
            }
            swap(classID, dayHour1, dayHour2);
        }
        else if(timetable[classID][dayHour1].get(0).getGroupID().startsWith("1")
                && timetable[classID][dayHour2].get(0).getValueOfFreeness() == 0) {
            ArrayList<Integer> others = findOtherGroup(classID, allClassGrades);
            boolean enableSwap = true;
            for (int id: others) {
                if(timetable[id][dayHour2].get(0).getValueOfFreeness() != 0) {
                    enableSwap = false;
                    break;
                }
            }
            if(enableSwap) {
                swap(classID, dayHour1, dayHour2);
                for (int id: others) {
                    swap(id, dayHour1, dayHour2);
                }
            }
        } else if(timetable[classID][dayHour2].get(0).getGroupID().startsWith("1")
                && timetable[classID][dayHour1].get(0).getValueOfFreeness() == 0) {
            ArrayList<Integer> others = findOtherGroup(classID, allClassGrades);
            boolean enableSwap = true;
            for (int id: others) {
                if(timetable[id][dayHour1].get(0).getValueOfFreeness() != 0) {
                    enableSwap = false;
                    break;
                }
            }
            if(enableSwap) {
                swap(classID, dayHour1, dayHour2);
                for (int id: others) {
                    swap(id, dayHour1, dayHour2);
                }
            }
        } else if(timetable[classID][dayHour1].get(0).getValueOfFreeness() == 0
                && timetable[classID][dayHour2].get(0).getValueOfFreeness() == 0) {
            swap(classID, dayHour1, dayHour2);
        }
    }
}