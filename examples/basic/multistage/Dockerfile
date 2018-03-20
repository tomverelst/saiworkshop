# Create a step named builder
FROM alpine as builder
# Create a file
RUN echo "Hello" >> "world.txt"

 # Create our final step
FROM alpine
# Copy the file from the previous step
COPY --from=builder world.txt hello.txt

CMD ["cat", "hello.txt"]
