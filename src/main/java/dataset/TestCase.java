package dataset;

public record TestCase(String testClassName, String testMethodName, String testCaseStr) {
    @Override
    public String toString() {
        return testCaseStr;
    }
}
