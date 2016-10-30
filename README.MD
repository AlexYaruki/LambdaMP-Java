## Lambda-MP-Java

This project aims to provide OpenMP-inspired API for multithreaded processing Java 8 Lambda Expressions.

In initial development, these parts from OpenMP 1.0 will be supported:
   * Directives:
     * omp parallel
     * omp sections
     * omp section
     * omp for
     * omp single
     * omp master
     * omp critical
     * omp barrier
     * omp ordered
     
   * Combined directives 
     * omp parallel for
     * omp parallel sections
    
   * Functions:
     * omp_set_num_threads
     * omp_get_num_threads
     * omp_get_max_threads
     * omp_get_thread_num
     * omp_get_num_procs
     * omp_get_in_parallel
     * omp_set_dynamic
     * omp_get_dynamic
    
   * Environment Variables
     * OMP_SCHEDULE
     * OMP_NUM_THREADS
     * OMP_DYNAMIC
     
Note: For now nested parallelism is not supported until above set of features are implemented