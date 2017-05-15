# Migration - OpenMP to LambdaMP-Java

### Tips:

1. Regions in OpenMP are represented as structured blocks:
    ```C
        #pragma omp parallel
        {
            // parallel region
        }
    ```
    Regions in LambdaMP-Java are represented as lambda expressions:
    ```java
       LMP.parallel(() -> {
           // parallel region
       })
    ```
    Invocation of "LMP.parallel" static method signals creation of parallel region, and lambda expressions represents body of region
   
2. Variable Capture

    * OpenMP: OpenMP Application Programming Interface, Version 4.5, &2.15 Data Environment
    * LambdaMP-Java: Java Language Specification, Java SE 8 Edition, &15.27.2 Lambda Body

3. OpenMP "master" clause: 
        LambdaMP-Java API differs from OpenMP standard in not having "thread". When thread encounters "LMP.parallel()" method, 
    creates additional threads equal to value returned by LMP.getThreadCount. Parallel region represented by lambda 
    expression or implementation of corresponding interface is pass to each thread. After starting these threads, 
    "main" thread starts waiting for all threads to finish. Alternative is to use "LMP.single()" method which result in 
    similar effect.

### Feature list
    
| OpenMP          | LambdaMP-Java                                                                |
|-----------------|------------------------------------------------------------------------------|
| omp parallel    | LMP.parallel                                                                 |
| -> if           | LMP.parallel(boolean isParallel,...)                                         |     
| -> num_threads  | LMP.parallel(int threadCount,...)                                            |
| -> default      | Not applicable, lookup Java 8 variable capture and Tips.2 - Variable Capture |
| -> private      | Not applicable, lookup Java 8 variable capture and Tips.2 - Variable Capture |
| -> firstprivate | Not applicable, lookup Java 8 variable capture and Tips.2 - Variable Capture |
| -> shared       | Not applicable, lookup Java 8 variable capture and Tips.2 - Variable Capture |
| -> copyin       |                                                                              |
| -> reduction    |                                                                              |
| -> proc_bind    | Not applicable                                                               |