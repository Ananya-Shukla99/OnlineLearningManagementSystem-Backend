#!/bin/bash

# Progress Service Test Verification Script
# Validates that all test fixes have been applied correctly

echo "================================================"
echo "Progress Service - Test Verification Script"
echo "================================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the correct directory
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}✗ Error: pom.xml not found. Please run this script from the progress-service directory.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found pom.xml - Running from correct directory${NC}"
echo ""

# Check if @WithMockUser has been added to test methods
echo "================================================"
echo "Checking for @WithMockUser annotations..."
echo "================================================"

TEST_FILE="src/test/java/com/edulearn/progress/controller/ProgressControllerTest.java"

if [ ! -f "$TEST_FILE" ]; then
    echo -e "${RED}✗ Test file not found: $TEST_FILE${NC}"
    exit 1
fi

# Count @WithMockUser annotations
MOCK_USER_COUNT=$(grep -c "@WithMockUser" "$TEST_FILE")

if [ "$MOCK_USER_COUNT" -ge 15 ]; then
    echo -e "${GREEN}✓ Found $MOCK_USER_COUNT @WithMockUser annotations (Expected: >= 15)${NC}"
else
    echo -e "${RED}✗ Only found $MOCK_USER_COUNT @WithMockUser annotations (Expected: >= 15)${NC}"
    exit 1
fi

echo ""

# Check for Spring Security Test import
echo "================================================"
echo "Checking for Spring Security Test import..."
echo "================================================"

if grep -q "import org.springframework.security.test.context.support.WithMockUser;" "$TEST_FILE"; then
    echo -e "${GREEN}✓ Found WithMockUser import${NC}"
else
    echo -e "${RED}✗ WithMockUser import not found${NC}"
    exit 1
fi

echo ""

# Run the specific failing test
echo "================================================"
echo "Running the previously failing test..."
echo "================================================"

echo "Test: testGetLessonProgressNotFound()"
echo "Command: mvn test -Dtest=ProgressControllerTest#testGetLessonProgressNotFound"
echo ""

mvn test -Dtest=ProgressControllerTest#testGetLessonProgressNotFound

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Test PASSED${NC}"
else
    echo ""
    echo -e "${RED}✗ Test FAILED${NC}"
    exit 1
fi

echo ""

# Run all controller tests
echo "================================================"
echo "Running all ProgressControllerTest tests..."
echo "================================================"

mvn test -Dtest=ProgressControllerTest

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ All controller tests PASSED${NC}"
else
    echo ""
    echo -e "${RED}✗ Some controller tests FAILED${NC}"
    exit 1
fi

echo ""

# Summary
echo "================================================"
echo "✓ VERIFICATION COMPLETE - ALL CHECKS PASSED"
echo "================================================"
echo ""
echo "Summary:"
echo "  ✓ @WithMockUser annotations added: $MOCK_USER_COUNT"
echo "  ✓ Spring Security Test imports present"
echo "  ✓ Previously failing test now passes"
echo "  ✓ All controller tests pass"
echo ""
echo "Next Steps:"
echo "  1. Run full test suite: mvn clean test"
echo "  2. Generate coverage report: mvn clean test jacoco:report"
echo "  3. Review test results in: target/surefire-reports/"
echo ""
echo "================================================"

