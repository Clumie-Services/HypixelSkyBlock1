#!/bin/bash

# Copy the Forwarding Secret
cp configuration_files/forwarding.secret ./forwarding.secret

# Ensure configuration directory exists
mkdir -p ./configuration

# If config.yml is missing in configuration folder, copy from example (which comes from host volume)
if [ ! -f ./configuration/config.yml ]; then
    echo "Copying config.example.yml to ./configuration/config.yml"
    cp configuration_files/config.example.yml ./configuration/config.yml
fi

# Update config.yml with the forwarding secret (velocity-secret)
secret=$(cat ./forwarding.secret)
echo "Injecting secret into ./configuration/config.yml"
sed -i "s/velocity-secret: .*/velocity-secret: '$secret'/" ./configuration/config.yml

# For debugging: verify the file content
echo "--- content of ./configuration/config.yml ---"
cat ./configuration/config.yml
echo "-------------------------------------------"

# Update settings.yml (NanoLimbo)
if [ -f ./settings.yml ]; then
    echo "Updating settings.yml"
    sed -i "s/secret: '.*'/secret: '$secret'/" ./settings.yml
    sed -i "s/ip: 'localhost'/ip: '0.0.0.0'/" ./settings.yml
fi

echo "Starting service: $SERVICE_CMD"
exec $SERVICE_CMD