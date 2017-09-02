local component = require("component")
local term = require("term")

if not term.isAvailable() then
  computer.beep()
  os.exit()
end

function error(message)
  component.gpu.setBackground(0x000000)
  component.gpu.setForeground(0xFF0000)
  local xt, yt = term.getCursor()
  component.gpu.set(xt, yt, message)
  component.gpu.setBackground(0x000000)
  component.gpu.setForeground(0xFFFFFF)
  print("")
end

if not component.isAvailable("warpdriveRadar") then
  error("No radar detected")
  os.exit()
end

local radar = component.warpdriveRadar

local argv = { ... }
if #argv ~= 1 then
  error("Usage: ping <scanRadius>")
  os.exit()
end
local radius = tonumber(argv[1])

if radius < 1 or radius > 9999 then
  error("Radius must be between 1 and 9999")
  os.exit()
end

local energy, energyMax = radar.energy()
local energyRequired = radar.getEnergyRequired(radius)
local scanDuration = radar.getScanDuration(radius)
if energy < energyRequired then
  error("Low energy level... (" .. energy .. "/" .. energyRequired .. ")")
  os.exit()
end
radar.radius(radius)
radar.start()
os.sleep(0.5)

print("Scanning... (" .. scanDuration .. " s)")
os.sleep(scanDuration)

local delay = 0
local count
repeat
  count = radar.getResultsCount()
  os.sleep(0.1)
  delay = delay + 1
until (count ~= nil and count ~= -1) or delay > 10

if count ~= nil and count > 0 then
  for i=0, count-1 do
    success, type, name, x, y, z = radar.getResult(i)
    if success then
      print(type .. " " .. name .. " @ (" .. x .. " " .. y .. " " .. z .. ")")
    else
      error("Error " .. type)
    end
  end
else
  print("Nothing was found =(")
end
