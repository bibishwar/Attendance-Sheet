package com.example.demo1;

import java.util.List;

/**
 * Default roster and subjects used the first time the application runs
 * (before students.csv and subjects.txt exist).
 */
public final class SeedData {

    private SeedData() {
    }

    public static List<Student> students() {
        return List.of(
                new Student("4420250902", "WEMEGAH STEPHANIE SEFAKOR"),
                new Student("4420250903", "BIADI MOHAMED"),
                new Student("4420250914", "BOUPPHANOUVONG ANOUSITH"),
                new Student("4420250944", "DURANTO THOMAS GOMES"),
                new Student("4420250945", "JOY JOSEPH COSTA"),
                new Student("4420250969", "Abbasi Memoona Zainab"),
                new Student("4420250970", "ISLAM MD ASHIKUL"),
                new Student("4420250973", "Mir Maryam Naveed"),
                new Student("4420250980", "Bai Lachmi"),
                new Student("4420250981", "JABOUR FATIMA ZAHRA"),
                new Student("4420250983", "ARMAN ARIFUL ISLAM"),
                new Student("4420250984", "Chakma Bibishwar"),
                new Student("4420250985", "CHAKMA SHELI"),
                new Student("4420250986", "PROTHOM MD TOFAEL SARKAR"),
                new Student("4420250989", "Nupur Tashnova Islam"),
                new Student("4420250990", "Seeam Sajim Mahmud"),
                new Student("4420250995", "BISWAS SHARMILA"),
                new Student("4420250996", "Aminjon Qurbonalizoda"));
    }

    public static List<String> subjects() {
        return List.of(
                "Probability theory",
                "Computer Organization Principles",
                "Data structure",
                "Basic Circuit Analysis",
                "Comprehensive Design of Data Structure",
                "Chinese 3");
    }
}
