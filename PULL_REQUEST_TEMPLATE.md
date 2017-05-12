**Developer Responsibility:**
- [ ] Code is tested adequately, ready for review
- [ ] JVM and container have been tuned with reasonable memory constraints
- [ ] Deployment is tested adequately
- [ ] SonarQube issues are resolved
- [ ] Pull Request description is adequate for reviewer to understand the change
- [ ] Merge conflicts are resolved

**Review Checklist:**
- [ ] Check for reasonable memory constraints JVMs and containers
- [ ] Automated tests are running successfully and passing
- [ ] Classes, methods and variables are descriptive and appropriate
- [ ] Code is clean, readable and narrative
- [ ] Minimal tight coupling, excessive code, inefficient code and unsafe code
- [ ] Appropriate documentation to clarify complex sections of code
- [ ] Appropriate documentation to describe camel routes and REST routes
- [ ] Changes are appropriate and meet the project's design and scope of work
- [ ] Readme is kept up to date

**Approval Checklist:**
- [ ] Ask developer to resolve any merge conflicts before approving, where necessary
- [ ] Document deferred review items in issue list, check for existing issues
- [ ] Perform merge once satisfied with the PR
- [ ] Prefer [rebase over merge](https://www.atlassian.com/git/tutorials/merging-vs-rebasing "atlassian.com"), where appropriate
- [ ] Remove feature branch once the rebase/merge is completed