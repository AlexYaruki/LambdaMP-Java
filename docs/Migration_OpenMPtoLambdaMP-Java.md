Migration - OpenMP to LambdaMP-Java

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
    
- [ ] Variable capture
- [ ] OpenMP Master Region