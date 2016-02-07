local component = require("component")
local computer = require("computer")
local term = require("term")
local event = require("event")
local fs = require("filesystem")
local serialization = require("serialization")

Style = {
  CDefault = 0xFFFFFF,
  BGDefault = 0x0000FF,

  CTitle = 0x000000,
  BGTitle = 0x00FFFF,

  CWarning = 0xFFFFFF,
  BGWarning = 0xFF0000,

  CSuccess = 0xFFFFFF,
  BGSuccess = 0x32CD32,

  CDisabled = 0x808080,
  BGDisabled = 0x0000FF
}

if not term.isAvailable() then
  computer.beep()
  return
end

----------- Monitor support

-- need to memorize colors so we can see debug stack dump
local gpu_frontColor = 0xFFFFFF
local gpu_backgroundColor = 0x000000
function SetMonitorColorFrontBack(frontColor, backgroundColor)
  gpu_frontColor = frontColor
  gpu_backgroundColor = backgroundColor
end

function Write(text)
  if term.isAvailable() then
    local w, h = component.gpu.getResolution()
    if w then
      local xt, yt = term.getCursor()
      component.gpu.setBackground(gpu_backgroundColor)
      component.gpu.setForeground(gpu_frontColor)
      component.gpu.set(xt, yt, text)
      SetCursorPos(xt + #text, yt)
      component.gpu.setBackground(0x000000)
    end
  end
end

function SetCursorPos(x, y)
  term.setCursor(x, y)
end

function SetColorDefault()
  SetMonitorColorFrontBack(Style.CDefault, Style.BGDefault)
end

function SetColorTitle()
  SetMonitorColorFrontBack(Style.CTitle, Style.BGTitle)
end

function SetColorWarning()
  SetMonitorColorFrontBack(Style.CWarning, Style.BGWarning)
end

function SetColorSuccess()
  SetMonitorColorFrontBack(Style.CSuccess, Style.BGSuccess)
end

function SetColorDisabled()
  SetMonitorColorFrontBack(Style.CDisabled, Style.BGDisabled)
end

function Clear()
  clearWarningTick = -1
  SetColorDefault()
  component.gpu.setBackground(gpu_backgroundColor)
  component.gpu.setForeground(gpu_frontColor)
  term.clear()
  component.gpu.setBackground(0x000000)
  SetCursorPos(1, 1)
end

function ClearLine()
  SetColorDefault()
  term.clearLine()
  SetCursorPos(1, 1)
end

function WriteLn(text)
  Write(text)
  local x, y = term.getCursor()
  local width, height = component.gpu.getResolution()
  if y > height - 1 then
    y = 1
  end
  SetCursorPos(1, y + 1)
end

function WriteCentered(y, text)
  if term.isAvailable() then
    local sizeX, sizeY = component.gpu.getResolution()
    if sizeX then
      component.gpu.setBackground(gpu_backgroundColor)
      component.gpu.setForeground(gpu_frontColor)
      component.gpu.set((sizeX - text:len()) / 2, y, text)
      component.gpu.setBackground(0x000000)
    end
  end
  local xt, yt = term.getCursor()
  SetCursorPos(1, yt + 1)
end

function ShowTitle(text)
  Clear()
  SetColorTitle()
  WriteCentered(1, text)
  SetColorDefault()
end

function ShowMenu(text)
  Write(text)
  local sizeX, sizeY = component.gpu.getResolution()
  local xt, yt = term.getCursor()
  for i = xt, sizeX do
    Write(" ")
  end
  SetCursorPos(1, yt + 1)
end

local clearWarningTick = -1
function ShowWarning(text)
  local sizeX, sizeY = component.gpu.getResolution()
  SetColorWarning()
  SetCursorPos((sizeX - text:len() - 2) / 2, sizeY)
  Write(" " .. text .. " ")
  SetColorDefault()
  clearWarningTick = 5
end
function ClearWarning()
  if clearWarningTick > 0 then
    clearWarningTick = clearWarningTick - 1
  elseif clearWarningTick == 0 then
    SetColorDefault()
    local sizeX, sizeY = component.gpu.getResolution()
    SetCursorPos(1, sizeY)
    ClearLine()
  end
end

----------- Formatting & popups

function FormatFloat(value, nbchar)
  local str = "?"
  if value ~= nil then
    str = string.format("%g", value)
  end
  if nbchar ~= nil then
    str = string.sub("               " .. str, -nbchar)
  end
  return str
end
function FormatInteger(value, nbchar)
  local str = "?"
  if value ~= nil then
    str = string.format("%d", value)
  end
  if nbchar ~= nil then
    str = string.sub("               " .. str, -nbchar)
  end
  return str
end

function boolToYesNo(bool)
  if bool then
    return "YES"
  else
    return "no"
  end
end

function readInputNumber(currentValue)
  local inputAbort = false
  local input = string.format(currentValue)
  if input == "0" then
    input = ""
  end
  local x, y = term.getCursor()
  repeat
    ClearWarning()
    SetColorDefault()
    SetCursorPos(x, y)
    Write(input .. "            ")
    input = string.sub(input, -9)

    local params = { event.pull() }
    local eventName = params[1]
    local address = params[2]
    if address == nil then address = "none" end
    if eventName == "key_down" then
      local char = params[3]
      local keycode = params[4]
      if char >= 49 and char <= 57 then -- 1 to 9
        input = input .. string.format(char - 48)
      elseif keycode >= 2 and keycode <= 10 then -- 1 to 9
        input = input .. string.format(keycode - 1)
      elseif char == 48 or keycode == 11 then -- 0
        input = input .. "0"
      elseif char == 8 then -- Backspace
        input = string.sub(input, 1, string.len(input) - 1)
      elseif char == 0 and keycode == 211 then -- Delete
        input = ""
      elseif char == 13 then -- Enter
        inputAbort = true
      elseif char ~= 0 then
        ShowWarning("Key " .. char .. " " .. keycode .. " is invalid")
      end
    elseif eventName == "key_up" then
    -- drop it
    elseif eventName == "interrupted" then
      inputAbort = true
    elseif not common_event(eventName, params[3]) then
      ShowWarning("Event '" .. eventName .. "', " .. address .. " is unsupported")
    end
  until inputAbort
  SetCursorPos(1, y + 1)
  if input == "" then
    return currentValue
  else
    return tonumber(input)
  end
end

function readInputText(currentValue)
  local inputAbort = false
  local input = string.format(currentValue)
  local x, y = term.getCursor()
  repeat
    ClearWarning()
    SetColorDefault()
    SetCursorPos(x, y)
    Write(input .. "                              ")
    input = string.sub(input, -30)

    local params = { event.pull() }
    local eventName = params[1]
    local address = params[2]
    if address == nil then address = "none" end
    if eventName == "key_down" then
      local char = params[3]
      local keycode = params[4]
      if char >= 32 and char <= 127 then -- any ASCII table minus controls and DEL
        input = input .. string.char(char)
      elseif char == 8 then -- Backspace
        input = string.sub(input, 1, string.len(input) - 1)
      elseif char == 0 and keycode == 211 then -- Delete
        input = ""
      elseif char == 13 then -- Enter
        inputAbort = true
      elseif char ~= 0 then
        ShowWarning("Key " .. char .. " " .. keycode .. " is invalid")
      end
    elseif eventName == "key_up" then
    -- drop it
    elseif eventName == "interrupted" then
      inputAbort = true
    elseif not common_event(eventName, params[3]) then
      ShowWarning("Event '" .. eventName .. "', " .. address .. " is unsupported")
    end
  until inputAbort
  SetCursorPos(1, y + 1)
  if input == "" then
    return currentValue
  else
    return input
  end
end

function readConfirmation()
  ShowWarning("Are you sure? (y/n)")
  repeat
    local params = { event.pull() }
    local eventName = params[1]
    local address = params[3]
    if address == nil then address = "none" end
    if eventName == "key_down" then
      local char = params[3]
      if char == 89 or char == 121 then -- Y
        return true
      else
        return false
      end
    elseif eventName == "key_up" then
    -- drop it
    elseif eventName == "interrupted" then
      return false
    elseif not common_event(eventName, params[3]) then
      ShowWarning("Event '" .. eventName .. "', " .. address .. " is unsupported")
    end
  until false
end

----------- commons: menu, event handlers, etc.

function common_event(eventName, param)
  if eventName == "redstone" then
  --    redstone_event(param)
  elseif eventName == "timer" then
  else
    return false
  end
  return true
end

function menu_common()
  SetCursorPos(1, 23)
  SetColorTitle()
  ShowMenu("0 Connections, 1 Ship core, X Exit")
end

----------- Configuration

function data_save()
  local file = fs.open("shipdata.txt", "w")
  if file ~= nil then
    file:write(serialization.serialize(data))
    file:close()
  else
    ShowWarning("No file system")
  end
end

function data_read()
  data = { }
  if fs.exists("shipdata.txt") then
    local file = fs.open("shipdata.txt", "r")
    local size = fs.size("shipdata.txt")
    local rawData = file:read(size)
    if rawData ~= nil then
      data = serialization.unserialize(rawData)
    end
    file:close()
  end
  if data.core_summon == nil then data.core_summon = false; end
  if data.core_distance == nil then data.core_distance = 0; end
  if data.core_direction == nil then data.core_direction = 0; end
end

function data_setName()
  ShowTitle("<==== Set name ====>")

  SetCursorPos(1, 2)
  Write("Enter ship name: ")
  label = readInputText(label)
  -- FIXME os.setComputerLabel(label)
  if ship ~= nil then
    ship.coreFrequency(label)
  end
  -- FIXME os.reboot()
end

----------- Ship support

core_front = 0
core_right = 0
core_up = 0
core_back = 0
core_left = 0
core_down = 0
core_isInHyper = false
core_shipLength = 0
core_realDistance = 0
core_jumpCost = 0
core_shipSize = 0

function core_boot()
  if ship == nil then
    return
  end

  Write("Booting Ship Core")

  if data.core_summon then
    ship.summon_all()
  end

  Write(".")
  core_front, core_right, core_up = ship.dim_positive()
  core_back, core_left, core_down = ship.dim_negative()
  core_isInHyper = ship.isInHyperspace()

  Write(".")
  repeat
    pos = ship.position()
    os.sleep(0.3)
  until pos ~= nil
  X, Y, Z = ship.position()
  Write(".")
  repeat
    isAttached = ship.isAttached()
    os.sleep(0.3)
  until isAttached ~= false

  Write(".")
  repeat
    core_shipSize = ship.getShipSize()
    os.sleep(0.3)
  until core_shipSize ~= nil

  Write(".")
  core_computeRealDistance()

  Write(".")
  ship.mode(1)
  WriteLn("")
end

function core_writeDirection()
  if data.core_direction == 1 then
    WriteLn(" Direction        = Up")
  elseif data.core_direction == 2 then
    WriteLn(" Direction        = Down")
  elseif data.core_direction == 0 then
    WriteLn(" Direction        = Front")
  elseif data.core_direction == 180 then
    WriteLn(" Direction        = Back")
  elseif data.core_direction == 90 then
    WriteLn(" Direction        = Left")
  elseif data.core_direction == 255 then
    WriteLn(" Direction        = Right")
  end
end

function core_computeRealDistance()
  if core_isInHyper then
    core_shipLength = 0
    core_realDistance = data.core_distance * 100 + core_shipLength
    ship.mode(2)
  else
    if data.core_direction == 1 or data.core_direction == 2 then
      core_shipLength = core_up + core_down + 1
    elseif data.core_direction == 0 or data.core_direction == 180 then
      core_shipLength = core_front + core_back + 1
    elseif data.core_direction == 90 or data.core_direction == 255 then
      core_shipLength = core_left + core_right + 1
    end
    core_realDistance = data.core_distance + core_shipLength - 1
    ship.mode(1)
  end
  core_jumpCost = ship.getEnergyRequired(core_realDistance)
end

function core_computeNewCoordinates(cx, cy, cz)
  local res = { x = cx, y = cy, z = cz }
  if data.core_direction == 1 then
    res.y = res.y + core_realDistance
  elseif data.core_direction == 2 then
    res.y = res.y - core_realDistance
  end
  local dx, dy, dz = ship.getOrientation()
  if dx ~= 0 then
    if data.core_direction == 0 then
      res.x = res.x + (core_realDistance * dx)
    elseif data.core_direction == 180 then
      res.x = res.x - (core_realDistance * dx)
    elseif data.core_direction == 90 then
      res.z = res.z + (core_realDistance * dx)
    elseif data.core_direction == 255 then
      res.z = res.z - (core_realDistance * dx)
    end
  else
    if data.core_direction == 0 then
      res.z = res.z + (core_realDistance * dz)
    elseif data.core_direction == 180 then
      res.z = res.z - (core_realDistance * dz)
    elseif data.core_direction == 90 then
      res.x = res.x + (core_realDistance * dz)
    elseif data.core_direction == 255 then
      res.x = res.x - (core_realDistance * dz)
    end
  end
  return res
end

function core_warp()
  -- rs.setOutput(alarm_side, true)
  if readConfirmation() then
    -- rs.setOutput(alarm_side, false)
    ship.direction(data.core_direction)
    ship.distance(data.core_distance)
    if core_isInHyper then
      ship.mode(2)
    else
      ship.mode(1)
    end
    ship.jump()
    -- ship = nil
  end
  -- rs.setOutput(alarm_side, false)
end

function core_page_setDistance()
  ShowTitle("<==== Set distance ====>")
  
  core_computeRealDistance()
  local maximumDistance = core_shipLength + 127
  local userEntry = core_realDistance
  if userEntry <= 1 then
    userEntry = 0
  end
  repeat
    SetCursorPos(1, 2)
    if core_isInHyper then
      Write("Distance * 100 (min " .. core_shipLength .. ", max " .. maximumDistance .. "): ")
    else
      Write("Distance (min " .. (core_shipLength + 1) .. ", max " .. maximumDistance .. "): ")
    end
    userEntry = readInputNumber(userEntry)
    if userEntry <= core_shipLength or userEntry > maximumDistance then
      ShowWarning("Wrong distance. Try again.")
    end
  until userEntry > core_shipLength and userEntry <= maximumDistance

  data.core_distance = userEntry - core_shipLength + 1
  core_computeRealDistance()
end

function core_page_setDirection()
  local inputAbort = false
  local drun = true
  repeat
    ShowTitle("<==== Set direction ====>")
    core_writeDirection()
    SetCursorPos(1, 19)
    SetColorTitle()
    ShowMenu("Use directional keys")
    ShowMenu("W/S keys for Up/Down")
    ShowMenu("Enter - confirm")
    SetColorDefault()
    local params = { event.pull() }
    local eventName = params[1]
    local address = params[2]
    if address == nil then address = "none" end
    if eventName == "key_down" then
      local char = params[3]
      local keycode = params[4]
      if keycode == 200 then
        data.core_direction = 0
      elseif keycode == 17 or keycode == 201 then
        data.core_direction = 1
      elseif keycode == 203 then
        data.core_direction = 90
      elseif keycode == 205 then
        data.core_direction = 255
      elseif keycode == 208 then
        data.core_direction = 180
      elseif keycode == 31 or keycode == 209 then
        data.core_direction = 2
      elseif keycode == 28 then
        inputAbort = true
      else
        ShowWarning("Key " .. char .. " " .. keycode .. " is invalid")
      end
    elseif eventName == "key_up" then
    -- drop it
    elseif eventName == "interrupted" then
      inputAbort = true
    elseif not common_event(eventName, params[3]) then
      ShowWarning("Event '" .. eventName .. "', " .. address .. " is unsupported")
    end
  until inputAbort
end

function core_page_setDimensions()
  ShowTitle("<==== Set dimensions ====>")
  Write(" Front (".. core_front ..") : ")
  core_front = readInputNumber(core_front)
  Write(" Right (".. core_right ..") : ")
  core_right = readInputNumber(core_right)
  Write(" Up    (".. core_up ..") : ")
  core_up = readInputNumber(core_up)
  Write(" Back  (".. core_back ..") : ")
  core_back = readInputNumber(core_back)
  Write(" Left  (".. core_left ..") : ")
  core_left = readInputNumber(core_left)
  Write(" Down  (".. core_down ..") : ")
  core_down = readInputNumber(core_down)
  Write("Setting dimensions...")
  ship.dim_positive(core_front, core_right, core_up)
  ship.dim_negative(core_back, core_left, core_down)
  core_shipSize = ship.getShipSize()
  if core_shipSize == nil then core_shipSize = 0 end
end

function core_page_summon()
  ShowTitle("<==== Summon players ====>")
  local playersString, playersArray = ship.getAttachedPlayers()
  for i = 1, #playersArray do
    WriteLn(i..". "..playersArray[i])
  end
  SetColorTitle()
  ShowMenu("Enter player number")
  ShowMenu("or press enter to summon everyone")
  SetColorDefault()

  Write(":")
  local input = readInputNumber("")
  if input == "" then
    ship.summon_all()
  else
    input = tonumber(input)
    ship.summon(input - 1)
  end
end

function core_page_jumpToBeacon()
  ShowTitle("<==== Jump to beacon ====>")

  Write("Enter beacon frequency: ")
  local freq = readInputText("")
  -- rs.setOutput(alarm_side, true)
  if readConfirmation() then
    -- rs.setOutput(alarm_side, false)
    ship.mode(4)
    ship.beaconFrequency(freq)
    ship.jump()
    -- ship = nil
  end
  -- rs.setOutput(alarm_side, false)
end

function core_page_jumpToGate()
  ShowTitle("<==== Jump to Jumpgate ====>")

  Write("Enter jumpgate name: ")
  local name = readInputText("")
  -- rs.setOutput(alarm_side, true)
  if readConfirmation() then
    -- rs.setOutput(alarm_side, false)
    ship.mode(6)
    ship.targetJumpgate(name)
    ship.jump()
    -- ship = nil
  end
  -- rs.setOutput(alarm_side, false)
end

function core_page()
  ShowTitle(label .. " - Ship status")
  if ship ~= nil then
    WriteLn("")
    X, Y, Z = ship.position()
    WriteLn("Core:")
    WriteLn(" x, y, z          = " .. X .. ", " .. Y .. ", " .. Z)
    local energy, energyMax = ship.energy()
    if energy == nil then energy = 0 end
    if energyMax == nil then energyMax = 1 end
    WriteLn(" Energy           = " .. math.floor(energy / energyMax * 100) .. " % (" .. energy .. "EU)")
    local playersString, playersArray = ship.getAttachedPlayers()
    if playersString == "" then players = "-" end
    WriteLn(" Attached players = " .. playersString)
    WriteLn("")
    WriteLn("Dimensions:")
    WriteLn(" Front, Right, Up = " .. FormatInteger(core_front) .. ", " .. FormatInteger(core_right) .. ", " .. FormatInteger(core_up))
    WriteLn(" Back, Left, Down = " .. FormatInteger(core_back) .. ", " .. FormatInteger(core_left) .. ", " .. FormatInteger(core_down))
    WriteLn(" Size             = " .. core_shipSize .. " blocks")
    WriteLn("")
    WriteLn("Warp data:")
    core_writeDirection()
    local dest = core_computeNewCoordinates(X, Y, Z)
    WriteLn(" Distance         = " .. core_realDistance .. " (" .. core_jumpCost .. "EU, " .. math.floor(energy / core_jumpCost) .. " jumps)")
    WriteLn(" Dest.coordinates = " .. FormatInteger(dest.x) .. ", " .. FormatInteger(dest.y) .. ", " .. FormatInteger(dest.z))
    if data.core_summon then
      WriteLn(" Summon after     = Yes")
    else
      WriteLn(" Summon after     = No")
    end
  else
    ShowWarning("No ship controller detected")
  end

  SetCursorPos(1, 19)
  SetColorTitle()
  ShowMenu("D - Dimensions, M - Toggle summon, N - Ship name")
  ShowMenu("S - Set Warp Data, J - Jump, G - Jump to JumpGate")
  ShowMenu("B - Jump to Beacon, H - Jump to Hyperspace")
  ShowMenu("C - summon Crew")
end

function core_key(char, keycode)
  if char == 83 or char == 115 then -- S
    core_page_setDirection()
    core_page_setDistance()
    data_save()
    return true
  elseif char == 77 or char == 109 then -- M
    if data.core_summon then
      data.core_summon = false
  else
    data.core_summon = true
  end
  data_save()
  return true
  elseif char == 68 or char == 100 then -- D
    core_page_setDimensions()
    data_save()
    return true
  elseif char == 74 or char == 106 then -- J
    core_warp()
    return true
  elseif char == 67 or char == 99 then -- C
    core_page_summon()
    return true
  elseif char == 66 or char == 98 then -- B
    core_page_jumpToBeacon()
    return true
  elseif char == 71 or char == 103 then -- G
    core_page_jumpToGate()
    return true
  elseif char == 72 or char == 104 then -- H
    -- rs.setOutput(alarm_side, true)
    if readConfirmation() then
      -- rs.setOutput(alarm_side, false)
      ship.mode(5)
      ship.jump()
      -- ship = nil
    end
    -- rs.setOutput(alarm_side, false)
    return true
  elseif char == 78 or char == 110 then
    data_setName()
    return true
  end
  return false
end

----------- Boot sequence
label = computer.address()
if not label then
  label = "" .. computer.address()
end

-- read configuration
data_read()

-- initial scanning
ShowTitle(label .. " - Connecting...")
WriteLn("")

ship = nil
for address, componentType in component.list() do
  os.sleep(0)
  if componentType == "warpdriveShipController" then
    WriteLn("Wrapping " .. componentType)
    ship = component.proxy(address)
  end
end
-- os.sleep(1)

if not computer.address() and ship ~= nil then
  data_setName()
end

-- peripherals status
function connections_page()
  ShowTitle(label .. " - Connections")

  WriteLn("")
  if ship == nil then
    SetColorDisabled()
    WriteLn("No ship controller detected")
  else
    SetColorSuccess()
    WriteLn("Ship controller detected")
  end
end

-- peripheral boot up
Clear()
connections_page()
SetColorDefault()
WriteLn("")
os.sleep(0)
core_boot()
os.sleep(0)

-- main loop
abort = false
refresh = true
page = connections_page
keyHandler = nil
repeat
  ClearWarning()
  if refresh then
    Clear()
    page()
    menu_common()
    refresh = false
  end
  params = { event.pull() }
  eventName = params[1]
  address = params[2]
  if address == nil then address = "none" end
  -- WriteLn("...")
  -- WriteLn("Event '" .. eventName .. "', " .. address .. ", " .. params[3] .. ", " .. params[4] .. " received")
  -- os.sleep(0.2)
  if eventName == "key_down" then
    char = params[3]
    keycode = params[4]
    if char == 88 or char == 120 then -- x for eXit
      abort = true
    elseif char == 48 or keycode == 11 then -- 0
      page = connections_page
      keyHandler = nil
      refresh = true
    elseif char == 49 or keycode == 2 then -- 1
      page = core_page
      keyHandler = core_key
      refresh = true
    elseif keyHandler ~= nil and keyHandler(char, keycode) then
      refresh = true
      os.sleep(0)
    elseif char == 0 then -- control chars
      refresh = false
      os.sleep(0)
    else
      ShowWarning("Key " .. char .. " " .. keycode .. " is invalid")
      os.sleep(0.2)
    end
    -- func(unpack(params))
    -- abort, refresh = false, false
  elseif eventName == "char" then
  -- drop it
  elseif eventName == "key_up" then
  -- drop it
  elseif eventName == "interrupted" then
    abort = true
  elseif not common_event(eventName, params[3]) then
    ShowWarning("Event '" .. eventName .. "', " .. address .. " is unsupported")
    refresh = true
    os.sleep(0.2)
  end
until abort

-- clear screens on exit
SetMonitorColorFrontBack(0xFFFFFF, 0x000000)
term.clear()
SetCursorPos(1, 1)
Write("")
