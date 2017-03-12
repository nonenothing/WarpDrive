local component = require("component")
local term = require("term")

if not component.isAvailable("warpdriveForceFieldProjector") then
  print("No force field projector detected")
else
  local projector = component.warpdriveForceFieldProjector
  projector.enable(false)
  os.sleep(1)
  status, isEnabled, isConnected, isPowered, shape, energy = projector.state()
  print("Projector is disabled")
  print()
  print(status)
end

print("")