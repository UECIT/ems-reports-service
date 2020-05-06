# CDS-Directory of Services
## Overview
This service provides an implementation of various encounter report transformations. It's one input is the FHIR Bundle Encounter Report and its output is various XML Reports.

Currently, it supports transformations into:
- ECDS
- IUCDS
  
## Source Code Location
The repository for this project is located in a public GitLab space here: https://gitlab.com/ems-test-harness/ems-reports-service

## Build Steps

This project is configured to run on port 8086. For local machines, this can be accessed at http://localhost:8086. To run the Reports Service, simply run the maven task:

```
mvn spring-boot:run
```
## Project Structure
### Implementation
The Reports Service is a Java Spring Application. It is split into two major layers:

- Controller - This contains the end-point from where the transformation is invoked
- Transformation Layer - This contains transformations from the Bundle Encounter Report to the two XML reports. Java classes for the XML model are deployed as a [bintray artifact](http://bintray.com/ems-test-harness).

There is also a service layer for miscellaneous services required for the transformations.

There are also packages for:

- Utilities
- Configuration (For the spring, security and fhir server)
- Logging

### Tests
One component test is provided as well as unit tests for each of the transformations.

## Licence

Unless stated otherwise, the codebase is released under [the MIT License][mit].
This covers both the codebase and any sample code in the documentation.

The documentation is [© Crown copyright][copyright] and available under the terms
of the [Open Government 3.0][ogl] licence.

[rvm]: https://www.ruby-lang.org/en/documentation/installation/#managers
[bundler]: http://bundler.io/
[mit]: LICENCE
[copyright]: http://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/
[ogl]: http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
