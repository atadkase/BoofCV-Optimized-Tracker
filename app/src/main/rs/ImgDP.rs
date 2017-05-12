#pragma version(1)
#pragma rs java_package_name(in.atadkase.boofcvbenchmark)

#pragma rs reduce(multFloat) accumulator(multFloatAccum) combiner(multFloatSum)


static void multFloatAccum(float *accum, float val) {
  *accum += val*val;
}

static void multFloatSum(float *accum, const float *val) {
  *accum += *val;
}