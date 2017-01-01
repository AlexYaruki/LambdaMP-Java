## Lambda-MP-Java

This project aims to provide OpenMP-inspired API for multithreaded processing Java 8 Lambda Expressions.

If migrating from OpenMP, read "docs/Migration_OpenMPtoLambdaMP-Java.md".

Current development for version "0.1" is focused on:

   * Directives:
     * LMP.parallel() => (omp parallel)
     * LMP.single() => (omp single)
     * LMP.sections() => (omp sections)
     * LMP.section() => (omp section)
     * LMP.barrier() => (omp barrier)
     * LMP.critical() => (omp critical)
   
   * Combined directives:
     * LMP.parallelSections() => (omp parallel sections)


   * Control API:
     * LMP.setThreadCount() => (omp_set_num_threads)
     * LMP.getThreadCount() => (omp_get_num_threads)
     * LMP.getThreadId() => (omp_get_thread_id)
     * LMP.inParallel() => (omp_get_in_parallel)
     * LMP.getProcessorCount() => (omp_get_num_procs)

   * Environment variables:
     * LMP_THREAD_COUNT => (OMP_NUM_THREADS)


Additionally to list of adaptations from OpenMP standard, exception handling model is also considered.