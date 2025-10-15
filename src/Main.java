import resources.Empleado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Empleado> empleados = new ArrayList<>();
        loadEmpleados(empleados);

        //To-do: Filtrar empleados por un atributo: departamento
        Predicate<Empleado> itDept = e -> "Informática".equalsIgnoreCase(e.getDepartamento());

        //To-do: Ordenar empleados por un atributo: Nombre
        Comparator<Empleado> porNombre = Comparator.comparing(Empleado::getNombre);

        //To-do: Generar un mapa que me permita tener como clave los departamentos y como valor el total de empleados por departamento
        Function<List<Empleado>, Map<String, Integer>> deptCount = list -> {
            Map<String, Integer> map = new HashMap<>();
            for (Empleado e : list) {
                map.merge(e.getDepartamento(), 1, Integer::sum);
            }
            return map;
        };
        //Con stream
        Function<List<Empleado>, Map<String, Integer>> deptCountS = list ->
                list.stream()
                        .collect(Collectors.groupingBy(
                                Empleado::getDepartamento,
                                Collectors.collectingAndThen(
                                        Collectors.counting(),
                                        Long::intValue
                                )
                ));


        //To-do: Mostrar empleados por un consumer: Contratados en determinado mes
        Consumer<Empleado> showIfJanuary = e -> {
            if (e.getFechaIng().getMonth() == Month.JANUARY) {
                System.out.println(e);
            }
        };

        //Con stream
        Consumer<Empleado> show = System.out::println;


        //Uso de las funciones
        //1. Predicate - Sin stream
        System.out.println("Predicate result");
        List<Empleado> itEmployees = new ArrayList<>();
        for (Empleado e : empleados) {
            if (itDept.test(e)) {
                itEmployees.add(e);
            }
        }
        System.out.println(itEmployees);
        //1. Predicate - Con stream
        List<Empleado> itEmpleados = empleados.stream()
                .filter(itDept)
                .toList();
        System.out.println(itEmpleados);


        //2. Comparator - Sin stream
        System.out.println("Comparator result");
        List<Empleado> orderEmpleados = new ArrayList<>(List.copyOf(empleados));
        orderEmpleados.sort(porNombre);
        System.out.println(orderEmpleados);
        //2. Comparator - Con stream
        List<Empleado> sorted = empleados.stream()
                .sorted(porNombre)
                .toList();
        System.out.println(sorted);
        //sorted.forEach(System.out::println);

        //3. Function - Sin stream
        System.out.println("Function result");
        Map<String, Integer> totalPorDept = deptCount.apply(empleados);
        //3. Function - Con stream
        Map<String, Integer> totalPorDeptS = deptCountS.apply(empleados);
        System.out.println(totalPorDept);

        //4. Consumer - Sin stream
        System.out.println("Consumer result");
        for (Empleado e : empleados) {
            showIfJanuary.accept(e);
        }
        //4. Consumer - Con stream
        empleados.stream()
                .filter(e -> e.getFechaIng().getMonth() ==  Month.JANUARY)
                .forEach(show);


        // ACTIVIDAD #1: Calcular estadísticas de salario (mínimo, máximo, promedio)
        System.out.println("\n--- ACTIVIDAD #1: Estadísticas de Salario ---");

        // Usando DoubleSummaryStatistics para obtener todas las estadísticas en una sola operación
        // Convertimos BigDecimal a double para poder usar summaryStatistics()
        var estadisticasSalario = empleados.stream()
                .filter(Empleado::getActive) // Solo empleados activos
                .mapToDouble(e -> e.getSalario().doubleValue())
                .summaryStatistics();

        System.out.println("Salario Mínimo: $" + estadisticasSalario.getMin());
        System.out.println("Salario Máximo: $" + estadisticasSalario.getMax());
        System.out.println("Salario Promedio: $" + String.format("%.2f", estadisticasSalario.getAverage()));
        System.out.println("Total de empleados activos: " + estadisticasSalario.getCount());

        // ACTIVIDAD #2: Agrupar por género y departamento
        System.out.println("\n--- ACTIVIDAD #2: Agrupación por Género y Departamento ---");

        // Agrupación multinivel: primero por género, luego por departamento
        Map<String, Map<String, List<Empleado>>> porGeneroYDept = empleados.stream()
                .filter(Empleado::getActive)
                .collect(Collectors.groupingBy(
                        Empleado::getGenero,
                        Collectors.groupingBy(Empleado::getDepartamento)
                ));

        // Mostrar resultados de forma organizada
        porGeneroYDept.forEach((genero, deptMap) -> {
            System.out.println("\nGénero: " + genero);
            deptMap.forEach((dept, empleadosList) -> {
                System.out.println("  Departamento: " + dept + " - " + empleadosList.size() + " empleado(s)");
                empleadosList.forEach(e -> System.out.println("    - " + e.getNombre() + " " + e.getApellido()));
            });
        });

        // ACTIVIDAD #3: Top 3 empleados con más antigüedad
        System.out.println("\n--- ACTIVIDAD #3: Top 3 Empleados con Más Antigüedad ---");

        // Ordenamos por fecha de ingreso ascendente (los más antiguos primero)
        List<Empleado> top3Antiguos = empleados.stream()
                .filter(Empleado::getActive)
                .sorted(Comparator.comparing(Empleado::getFechaIng))
                .limit(3)
                .toList();

        top3Antiguos.forEach(e -> {
            long anios = java.time.Period.between(e.getFechaIng(), LocalDate.now()).getYears();
            System.out.println(e.getNombre() + " " + e.getApellido() +
                             " - Fecha de ingreso: " + e.getFechaIng() +
                             " (" + anios + " años de antigüedad)");
        });

        // ACTIVIDAD #4: Mapa con porcentaje de empleados activos por departamento
        System.out.println("\n--- ACTIVIDAD #4: Porcentaje de Empleados Activos por Departamento ---");

        // Primero obtenemos el total de empleados por departamento
        Map<String, Long> totalPorDepartamento = empleados.stream()
                .collect(Collectors.groupingBy(
                        Empleado::getDepartamento,
                        Collectors.counting()
                ));

        // Luego obtenemos los activos por departamento
        Map<String, Long> activosPorDepartamento = empleados.stream()
                .filter(Empleado::getActive)
                .collect(Collectors.groupingBy(
                        Empleado::getDepartamento,
                        Collectors.counting()
                ));

        // Calculamos el porcentaje de activos
        Map<String, Double> porcentajeActivosPorDept = totalPorDepartamento.keySet().stream()
                .collect(Collectors.toMap(
                        dept -> dept,
                        dept -> {
                            long total = totalPorDepartamento.get(dept);
                            long activos = activosPorDepartamento.getOrDefault(dept, 0L);
                            return (activos * 100.0) / total;
                        }
                ));

        porcentajeActivosPorDept.forEach((dept, porcentaje) ->
            System.out.println(dept + ": " + String.format("%.2f", porcentaje) + "% activos " +
                             "(" + activosPorDepartamento.getOrDefault(dept, 0L) + "/" +
                             totalPorDepartamento.get(dept) + " empleados)")
        );

        // ACTIVIDAD #5: Comparación de rendimiento stream() vs parallelStream()
        System.out.println("\n--- ACTIVIDAD #5: Comparación de Rendimiento ---");
        System.out.println("Generando lista de 5 millones de empleados con datos aleatorios...");

        List<Empleado> empleadosGrandes = generarEmpleadosAleatorios(5_000_000);

        System.out.println("Lista generada. Ejecutando pruebas de rendimiento...\n");

        // Test 5a: Calcular salario promedio con stream()
        long inicioStream = System.currentTimeMillis();
        double promedioStream = empleadosGrandes.stream()
                .mapToDouble(e -> e.getSalario().doubleValue())
                .average()
                .orElse(0.0);
        long finStream = System.currentTimeMillis();
        long tiempoStream = finStream - inicioStream;

        System.out.println("5a) Salario Promedio usando stream():");
        System.out.println("    Resultado: $" + String.format("%.2f", promedioStream));
        System.out.println("    Tiempo de ejecución: " + tiempoStream + " ms");

        // Test 5a: Calcular salario promedio con parallelStream()
        long inicioParallel = System.currentTimeMillis();
        double promedioParallel = empleadosGrandes.parallelStream()
                .mapToDouble(e -> e.getSalario().doubleValue())
                .average()
                .orElse(0.0);
        long finParallel = System.currentTimeMillis();
        long tiempoParallel = finParallel - inicioParallel;

        System.out.println("\n5a) Salario Promedio usando parallelStream():");
        System.out.println("    Resultado: $" + String.format("%.2f", promedioParallel));
        System.out.println("    Tiempo de ejecución: " + tiempoParallel + " ms");

        // Test 5b: Contar empleados por departamento con stream()
        long inicioStream2 = System.currentTimeMillis();
        Map<String, Long> conteoStream = empleadosGrandes.stream()
                .collect(Collectors.groupingBy(
                        Empleado::getDepartamento,
                        Collectors.counting()
                ));
        long finStream2 = System.currentTimeMillis();
        long tiempoStream2 = finStream2 - inicioStream2;

        System.out.println("\n5b) Cantidad de Empleados por Departamento usando stream():");
        conteoStream.forEach((dept, count) ->
            System.out.println("    " + dept + ": " + count + " empleados")
        );
        System.out.println("    Tiempo de ejecución: " + tiempoStream2 + " ms");

        // Test 5b: Contar empleados por departamento con parallelStream()
        long inicioParallel2 = System.currentTimeMillis();
        Map<String, Long> conteoParallel = empleadosGrandes.parallelStream()
                .collect(Collectors.groupingBy(
                        Empleado::getDepartamento,
                        Collectors.counting()
                ));
        long finParallel2 = System.currentTimeMillis();
        long tiempoParallel2 = finParallel2 - inicioParallel2;

        System.out.println("\n5b) Cantidad de Empleados por Departamento usando parallelStream():");
        conteoParallel.forEach((dept, count) ->
            System.out.println("    " + dept + ": " + count + " empleados")
        );
        System.out.println("    Tiempo de ejecución: " + tiempoParallel2 + " ms");

    }

    /**
     * Método auxiliar para generar empleados aleatorios para las pruebas de rendimiento
     */
    public static List<Empleado> generarEmpleadosAleatorios(int cantidad) {
        Random random = new Random();
        String[] nombres = {"Juan", "María", "Carlos", "Ana", "Luis", "Laura", "Pedro", "Sofia"};
        String[] apellidos = {"García", "Rodríguez", "Martínez", "López", "González", "Pérez"};
        String[] generos = {"M", "F"};
        String[] departamentos = {"Informática", "Contabilidad", "Talento Humano", "Ventas"};
        String[] cargos = {"Desarrollador", "Asistente", "Supervisor", "Gerente"};

        List<Empleado> lista = new ArrayList<>(cantidad);

        for (int i = 0; i < cantidad; i++) {
            String nombre = nombres[random.nextInt(nombres.length)];
            String apellido = apellidos[random.nextInt(apellidos.length)];
            String genero = generos[random.nextInt(generos.length)];
            String departamento = departamentos[random.nextInt(departamentos.length)];
            String cargo = cargos[random.nextInt(cargos.length)];
            BigDecimal salario = new BigDecimal(500 + random.nextInt(2000));
            LocalDate fechaIng = LocalDate.of(2018 + random.nextInt(6), 1 + random.nextInt(12), 1 + random.nextInt(28));

            lista.add(new Empleado(nombre, apellido, genero, departamento, cargo, salario, fechaIng));
        }

        return lista;
    }

    public static void loadEmpleados(List<Empleado> empleadoList){
        empleadoList.add(new Empleado("María", "Rodríguez", "F", "Contabilidad", "Asistente Contable", new BigDecimal(700), LocalDate.parse("2021-04-01")));
        empleadoList.add(new Empleado("Juan", "Gutierrez", "M", "Talento Humano", "Reclutador", new BigDecimal(500), LocalDate.parse("2023-03-11"), LocalDate.parse("2024-04-01"), false));
        empleadoList.add(new Empleado("José", "Albornoz", "M","Contabilidad", "Asistente Contable", new BigDecimal(800), LocalDate.parse("2020-08-15"), LocalDate.parse("2023-05-01"), false));
        empleadoList.add(new Empleado("Julián", "Flores", "M", "Informática", "Soporte TI", new BigDecimal(800), LocalDate.parse("2023-11-01")));
        empleadoList.add(new Empleado("Camila", "Mendoza","F", "Informática", "Desarrollador UI/UX", new BigDecimal(1000), LocalDate.parse("2021-07-08")));
        empleadoList.add(new Empleado("Camilo", "López", "M", "Contabilidad", "Supervisor Contable", new BigDecimal(1500), LocalDate.parse("2020-04-11")));
        empleadoList.add(new Empleado("Manuel", "Játiva", "M", "Contabilidad", "Asistente Contable", new BigDecimal(850), LocalDate.parse("2023-06-03")));
        empleadoList.add(new Empleado("Carlos", "Franco", "M", "Talento Humano", "Reclutador", new BigDecimal(650), LocalDate.parse("2023-01-07"), LocalDate.parse("2024-12-09"), false));
        empleadoList.add(new Empleado("Raúl", "Echeverría", "M", "Informática", "Infraestructura TI", new BigDecimal(950), LocalDate.parse("2020-02-14")));
        empleadoList.add(new Empleado("Estefanía", "Mendoza", "F", "Talento Humano", "Supervisora TH", new BigDecimal(1600), LocalDate.parse("2021-09-21")));
        empleadoList.add(new Empleado("Julie", "Flores", "F", "Informática", "Desarrollador", new BigDecimal(1200), LocalDate.parse("2021-12-10")));
        empleadoList.add(new Empleado("Melissa", "Morocho", "F","Contabilidad", "Asistente Contable", new BigDecimal(820), LocalDate.parse("2022-05-22"), LocalDate.parse("2023-07-09"), false));
        empleadoList.add(new Empleado("Camila", "Mendez", "F", "Contabilidad", "Asistente Cuentas", new BigDecimal(860), LocalDate.parse("2020-10-01")));
        empleadoList.add(new Empleado("José", "Rodríguez", "M","Informática", "Tester QA", new BigDecimal(1100), LocalDate.parse("2021-10-01")));
        empleadoList.add(new Empleado("Esteban", "Gutierrez","M", "Talento Humano", "Reclutador", new BigDecimal(700), LocalDate.parse("2023-04-01")));
        empleadoList.add(new Empleado("María", "López","F", "Contabilidad", "Asistente Contable", new BigDecimal(840), LocalDate.parse("2020-02-20"), LocalDate.parse("2024-07-15"), false));
        empleadoList.add(new Empleado("Cecilia", "Marín","F", "Informática", "Supervisora TI", new BigDecimal(2000), LocalDate.parse("2020-04-21")));
        empleadoList.add(new Empleado("Edison", "Cáceres","M", "Informática", "Desarrollador TI", new BigDecimal(1300), LocalDate.parse("2023-07-07")));
        empleadoList.add(new Empleado("María", "Silva", "F","Contabilidad", "Asistente Contable", new BigDecimal(900), LocalDate.parse("2021-11-15"), LocalDate.parse("2022-08-09"), false));

    }



}