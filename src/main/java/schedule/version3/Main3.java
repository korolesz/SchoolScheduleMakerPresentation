package schedule.version3;

import schedule.version3.data.Classes;
import schedule.version3.data.DataScan;

import java.util.List;



/*******************************************************************
 * TOVÁBBI MEGOLDANDÓ FELADATOK MÉG A PROJEKT KAPCSÁN
 *  - Óraadó tanár csak bizonyos napokon dolgozhasson, amit ráadásul előre meg lehet adni (hogy mikor szeretne)
 *  - Lehessen egy tanár órarendjét csak 4 napra elosztani és így az egyik napja üres legyen.
 *        (valamelyik nap továbbképzére jár egész évben, mestertanár stb)
 *  - Lehessen beállítani, hogy lehessen-e nulladik óra, vagy nem.
 *  - Mutációt módosítani úgy, hogy lehessen egy órá valamelyik nap végére tenni és helyére valamelyik nap végéről órát betenni
 *  - Fitness függvény módosítása speciális terem ütközések figyelembevételére (2 infó terem van, de 3 infó óra egyszerre)


 *  - FRONTENDET ÉPÍTENI
 *  - Ha megvan a frontend akkor egy olyan felület létrehozása (is) ahol az adat bevitel megtörténik
 *       // és ezek elmentése egy adatbázisba, majd a DataScan mdosítása, hogy a beolvasás az adatbázisból történjen.
 *  - Jó lenne megvalósítani, hogy a kész órarendet pdf-be lehessen konvertálni a frontend felületen valamilyen táblázatos nézetben.
 *  - Tesztelni igazi iskolákkal a kész verziót. (párral jó lenne kipróbálni)
 */

public class Main3 {
    DataScan dataScan = new DataScan();

    public static void main(String[] args) {
        new Main3().run();
    }

    private void run() {
        // Create genetic algorithm object
        GeneticAlgorithm ga = new GeneticAlgorithm(2000, 0.5, 0.5, 0, 20);

        // Scan input data, creating teachers, lessons and classes instances
        dataScan.scanData();

        // Initialize population using scanned data
        // Evaluating fitness of individuals during construction
        Population population = ga.initPopulation(dataScan.getAllClasses(), dataScan.getAllClassesGrades());

        //printTimeTable(dataScan.getAllClasses(), population.getFittest(0));

        // Keep track of current generation
        int generation = 1;

        /**
         * Start the evolution loop
         *
         * Every genetic algorithm problem has different criteria for finishing.
         * In this case, we know what a perfect solution looks like (we don't
         * always!), so our isTerminationConditionMet method is very
         * straightforward: if there's a member of the population whose
         * chromosome is all ones, we're done!
         */
        while (ga.isTerminationConditionMet(population) == false) {
            // Print fitness of fittest individual of each generation
            System.out.println();
            System.out.println("Best solution in " + generation + " generations: ");
            System.out.println(population.getFittest(0).getFitness());

            // Apply crossover to the population
            population = ga.crossoverPopulation(population);

            // Apply mutation to the population
            population = ga.mutatePopulation(population);


            // Increment the current generation
            generation++;

            // print timetable
            // printTimeTable(dataScan.getAllClasses(), population.getFittest(0));

            // break
            if (generation > 2000) {
                break;
            }
        }

        /**
         * We're out of the loop now, which means we have a perfect solution on
         * our hands. Let's print it out to confirm that it is actually all
         * ones, as promised.
         */
        System.out.println();
        System.out.println("__________________________________________________");
        System.out.println("__________________________________________________");
        System.out.println("Found solution in " + generation + " generations");
        System.out.println();
        printTimeTable(dataScan.getAllClasses(), population.getFittest(0));
    }


    public static void printTimeTable(List<Classes> allClasses, Individual individual) {
        for (int i = 0; i < allClasses.size(); i++) {
            System.out.println("\n\nA " + allClasses.get(i).getClassName() + " osztály órarendje:");
            for (int k = 0; k < individual.getTimetable()[i].length; k++) {
                if (k % 9 == 0) {
                    System.out.println();
                }
                System.out.print(k % 9 + ". óra: ");
                for (int j = 0; j < individual.getTimetable()[i][k].size(); j++) {
                    System.out.print(individual.getTimetable()[i][k].get(j).getGroupID() + " " + individual.getTimetable()[i][k].get(j).getNameOfLesson() + " - " + individual.getTimetable()[i][k].get(j).getTeacher()+",");
                }
                System.out.println();


            }
        }
    }

}