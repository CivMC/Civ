from ConfigObjects import Recipe, Enchantment, ItemStack, Factory, CraftedRecipe
from ParseConfig import ParseConfig

coeffs={}
gMod=0.1
mMod=0.1

def main():
    print 'Running....'
    ItemStack.importMaterials()
    Enchantment.importEnchantments()

    createConfigFile()

def createFactorieAndRecipes():
    inputs={}
    outputs={}
    enchantments={}
    recipes={}
    factories={}
    #Equipment
    enchantmentData=[]
    enchantmentData.extend([('Unbreaking',[(3,1)]),('Silk Touch',[(1,0.1)]),('Efficiency',[(1,.3),(2,.2),(3,0.1),(4,0.05),(5,0.01)])])
    enchantmentData.extend([('Bane of the Anthropods',[(1,.4),(2,.3),(3,.2),(4,.1),(5,0.3)]),('Smite',[(1,.4),(2,.3),(3,.2),(4,.1),(5,0.05)]),('Looting',[(1,0.5),(2,0.4),(3,0.3)])])
    enchantmentData.extend([('Respiration',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Blast Protection',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Feather Falling',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Fire Protection',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Projectile Protection',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)])])
    enchantmentsInputs=sum([[Enchantment(name=name,level=level,probability=prob) for level,prob in pairs] for name,pairs in enchantmentData],[])

    inputDict={'Iron':'Iron Ingot','Gold':'Gold Ingot','Diamond':'Diamond'}
    coeffs['t']={'Iron':2,'Gold':4,'Diamond':8}
    coeffs['i']={'Helmet':5,'Chestplate':8,'Leggings':7,'Boots':4,'Sword':2,'Axe':3,'Pickaxe':3,'Spade':1,'Hoe':2}# Modifier for different branches of the tree, based on vanilla costs
    coeffs['b']={'Helmet':1,'Chestplate':1,'Leggings':1,'Boots':1,'Sword':1,'Axe':1,'Pickaxe':1,'Spade':1,'Hoe':1}
    for key,value in coeffs['b'].items():coeffs['b'][key]=value*10
    coeffs['e']={'Helmet':1,'Chestplate':1,'Leggings':1,'Boots':1,'Sword':1,'Axe':1,'Pickaxe':1,'Spade':1,'Hoe':1}
    for key,value in coeffs['e'].items():coeffs['e'][key]=value*0.5
    for tech in coeffs['t'].keys():
        for equipment in coeffs['i'].keys():
            enchantments[tech+'_'+equipment]=[]
            if tech=='Gold':
                enchantments[tech+'_'+equipment]=list(enchantmentsInputs)
            inputs[tech+'_'+equipment]=[ItemStack(name=inputDict[tech],amount=coeffs['i'][equipment]*coeffs['b'][equipment]*coeffs['e'][equipment])]
            outputs[tech+'_'+equipment]=[ItemStack(name=tech+' '+equipment,amount=coeffs['b'][equipment])]
            recipes[tech+'_'+equipment]=Recipe(identifier=tech+'_'+equipment,name='Forge '+tech+' '+equipment+'.',inputs=inputs[tech+'_'+equipment],outputs=outputs[tech+'_'+equipment],enchantments=enchantments[tech+'_'+equipment])
            inputs[tech+'_'+equipment+'_Smithy']=[input.modifyAmount(1) for input in inputs[tech+'_'+equipment]]
            factories[tech+'_'+equipment+'_Smithy']=Factory(identifier=tech+'_'+equipment+'_Smithy',name=tech+' '+equipment+' Smithy',inputs=inputs[tech+'_'+equipment+'_Smithy'],outputRecipes=[recipes[tech+'_'+equipment]])
    #Food
    oi={('Bread',1):[('Wheat',3)],('Baked Potato',1):[('Potato',1)],('Cooked Chicken',1):[('Raw Chicken',1)],('Cooked Beef',1):[('Raw Beef',1)],('Grilled Pork',1):[('Pork',1)],('Cooked Fish',1):[('Raw Fish',1)],('Cookie',8):[('Wheat',2),('Cocoa',1)]}
    bulk=64
    eff=0.7
    for output in oi.keys():
        id=output[0].replace(' ','_')
        inputs[id]=[]
        for input in oi[output]:
            inputs[id].append(ItemStack(name=input[0],amount=input[1]*bulk*eff))
        outputs[id]=[ItemStack(name=output[0],amount=output[1]*bulk)]
        recipes[id]=Recipe(identifier=id,name=output[0],inputs=inputs[id],outputs=outputs[id])
        inputs[id+'_Bakery']=[itemStack.modifyAmount(20*gMod) for itemStack in inputs[id]]
        factories[id+'_Bakery']=Factory(identifier=id+'_Bakery',name=output[0]+' Bakery',inputs=inputs[id+'_Bakery'],outputRecipes=[recipes[id]])
    
    #Enchanting
    inputs['Wood_Cauldron']=[ItemStack(name='Stick',amount=64)]
    inputs['Diamond_Cauldron']=[ItemStack(name='Diamond',amount=20*gMod)]
    inputs['Emerald_Cauldron']=[ItemStack(name='Emerald',amount=20*gMod)]
    factories['Wood_Cauldron']=Factory(identifier='Wood_Cauldron',name='Wood Cauldron',inputs=inputs['Wood_Cauldron'])
    factories['Diamond_Cauldron']=Factory(identifier='Diamond_Cauldron',name='Diamond Cauldron',inputs=inputs['Diamond_Cauldron'])
    factories['Emerald_Cauldron']=Factory(identifier='Emerald_Cauldron',name='Emerald Cauldron',inputs=inputs['Emerald_Cauldron'])
    cauldronInputs={}
    cauldronInputs['Wood']=[([('Glass Bottle',1),('Wheat',40)],1),([('Glass Bottle',1),('Carrot',40)],1),([('Glass Bottle',1),('Baked Potato',40)],1),([('Glass Bottle',1),('Cookie',40)],1)]
    cauldronInputs['Diamond']=[]
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Red Rose',1),('Bread',1),('Cactus',3),('Grilled Pork',1),('Red Mushroom',1),('Rotten Flesh',1)],1))
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Yellow Flower',1),('Baked Potato',4),('Sugar Cane',3),('Cooked Chicken',1),('Brown Mushroom',1),('Rotten Flesh',1)],1))
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Red Rose',1),('Carrot',4),('Melon Block',3),('Cooked Beef',1),('Nether Warts',5),('Spider Eye',1)],1))
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Yellow Flower',1),('Cookie',4),('Pumpkin',3),('Cooked Fish',1),('Brown Mushroom',1),('Spider Eye',1)],1))
    cauldronInputs['Emerald']=[]
    cauldronInputs['Emerald'].append(([('Glass Bottle',4),('Red Rose',1),('Bread',1),('Cactus',3),('Grilled Pork',1),('Red Mushroom',1),('Carrot',4),('Melon Block',3),('Cooked Beef',1),('Nether Warts',1),('Grass',5),('Rotten Flesh',1),('Spider Eye',1)],4))
    cauldronInputs['Emerald'].append(([('Glass Bottle',4),('Yellow Flower',1),('Baked Potato',4),('Sugar Cane',3),('Cooked Chicken',1),('Red Mushroom',1),('Cookie',4),('Pumpkin',3),('Cooked Fish',1),('Brown Mushroom',1),('Vine',1),('Rotten Flesh',1),('Spider Eye',1)],4))
    for cauldron in cauldronInputs.keys():
        i=0
        for recipeInput,bottles in cauldronInputs[cauldron]:
            id=cauldron+'_XP_Bottle_'+str(i)
            i+=1
            inputs[id]=[ItemStack(name=name,amount=amount) for name,amount in recipeInput]
            outputs[id]=[ItemStack(name='Exp Bottle',amount=bottles)]
            recipes[id]=Recipe(identifier=id,name='Brew XP Bottles  - '+str(i),inputs=inputs[id],outputs=outputs[id])
            factories[cauldron+'_Cauldron'].addRecipe(recipes[id])
            
            inputs[id+'_Bulk']=[itemStack.modifyAmount(64) for itemStack in recipes[id].inputs]
            outputs[id+'_Bulk']=[itemStack.modifyAmount(64) for itemStack in recipes[id].outputs]
            recipes[id+'_Bulk']=Recipe(identifier=id+'_Bulk',name='Brew XP Bottles  - '+str(i),inputs=inputs[id+'_Bulk'],outputs=outputs[id+'_Bulk'],time=128)
            factories[cauldron+'_Cauldron'].addRecipe(recipes[id+'_Bulk'])
    #Wool
    inputColors=['White', 'Light Gray', 'Gray', 'Black', 'Brown', 'Pink']
    dyes={'Light Gray':'Light Gray Dye','Gray':'Gray Dye','Black':'Ink Sack','Red':'Rose Red','Orange':'Orange Dye','Yellow':'Dandelion Yellow','Lime':'Lime Dye','Green':'Cactus Green','Cyan':'Cyan Dye','Light Blue':'Light Blue Dye','Blue':'Lapis Lazuli','Purple':'Purple Dye','Magenta':'Magenta Dye','Pink':'Pink Dye','Brown':'Cocoa'}
    for outputColor in dyes.keys():
        factoryId=outputColor.replace(' ','_')+'_Wool_Processing'
        inputs[factoryId]=[ItemStack(name=dyes[outputColor],amount=256*gMod)]
        factories[factoryId]=Factory(identifier=factoryId,name=outputColor+' Wool Processing',inputs=inputs[factoryId])
        for inputColor in inputColors:
            if inputColor!=outputColor:
                id=inputColor.replace(' ','_')+'_Wool_'+outputColor
                inputs[id]=[ItemStack(name=inputColor+' Wool',amount=64),ItemStack(name=dyes[outputColor],amount=4)]
                outputs[id]=[ItemStack(name=outputColor+' Wool',amount=64)]
                recipes[id]=Recipe(identifier=id,name='Dye '+inputColor+' Wool '+outputColor,inputs=inputs[id],outputs=outputs[id])
                factories[factoryId].addRecipe(recipes[id])
    
    #Smelting
    #Stone
    id='Smelt_Stone'
    inputs[id]=[ItemStack(name='Cobblestone',amount=560)]
    outputs[id]=[ItemStack(name='Stone',amount=640)]
    recipes[id]=Recipe(identifier=id,name='Smelt Stone',inputs=inputs[id],outputs=outputs[id],time=120)
    id='Stone_Smelter'
    inputs[id]=[ItemStack(name='Stone',amount=2000*gMod)]
    factories[id]=Factory(identifier=id,name='Stone Smelter',inputs=inputs[id],outputRecipes=[recipes['Smelt_Stone']])
    #Charcoal
    woods=['Oak Wood','Spruce Wood','Birch Wood','Jungle Wood']
    id='Charcoal_Smelter'
    inputs[id]=[ItemStack(name='Charcoal',amount=1000*gMod)]
    factories[id]=Factory(identifier=id,name='Charcoal Burner',inputs=inputs[id])
    for wood in woods:
        id='Smelt_'+wood.replace(' ','_')
        inputs[id]=[ItemStack(name=wood,amount=280)]
        outputs[id]=[ItemStack(name='Charcoal',amount=320)]
        recipes[id]=Recipe(identifier=id,name='Burn '+wood,inputs=inputs[id],outputs=outputs[id],time=60)
        factories['Charcoal_Smelter'].addRecipe(recipes[id])
    #1:2.5 yield
    ores={'Coal Ore':'Coal','Iron Ore':'Iron Ingot','Gold Ore':'Gold Ingot','Diamond Ore':'Diamond','Quartz Ore':'Quartz'}
    mod={'Coal Ore':2.5,'Iron Ore':1.25,'Gold Ore':1.25,'Diamond Ore':2.5,'Quartz Ore':2.5}
    bulk=64
    for ore in ores.keys():
        #Recipe
        id='Smelt_'+ore.replace(' ','_')
        inputs[id]=[ItemStack(name=ore,amount=bulk)]
        outputs[id]=[ItemStack(name=ores[ore],amount=mod[ore]*bulk)]
        recipes[id]=Recipe(identifier=id,name='Smelt '+ore,inputs=inputs[id],outputs=outputs[id],time=bulk/8*2)
        #Factory
        id=ore.replace(' ','_')+'_Smelter'
        inputs[id]=[ItemStack(name=ore,amount=300*gMod)]
        factories[id]=Factory(identifier=id,name=ore+' Smelter',inputs=inputs[id],outputRecipes=[recipes['Smelt_'+ore.replace(' ','_')]])
    
    #Add in repair
    for factory in factories.values():
        factory.repairMultiple=min([input.amount for input in [input.modifyAmount(mMod) for input in factory.inputs]])
        factory.repairInputs=[input.modifyAmount(1.0/factory.repairMultiple) for input in [input.modifyAmount(mMod) for input in factory.inputs]]
    return (factories,recipes)

def createCraftingRecipes():
        enabledRecipes=[]
        enabledRecipes.append(CraftedRecipe('XP to Emerald',inputs={'a':ItemStack('Exp Bottle',amount=9)},output=ItemStack('Emerald')))
        enabledRecipes.append(CraftedRecipe('Emerald to XP',inputs={'a':ItemStack('Emerald')},output=ItemStack('Exp Bottle',amount=9)))
        enabledRecipes.append(CraftedRecipe('Stone to Double Slab',inputs={'s':ItemStack('Stone')},shape=['sss','sss'],output=ItemStack('Double Stone Slab')))
        enabledRecipes.append(CraftedRecipe('Slab to Double Slab',inputs={'s':ItemStack('Stone Slab')},shape=['s','s'],output=ItemStack('Double Stone Slab')))
        return enabledRecipes
def createConfigFile():
    config={}
    config['copy_defaults']='true'
    config['central_block']='WORKBENCH'
    config['save_cycle']='15'
    config['return_build_materials']='false'
    config['citadel_enabled']='true'
    config['factory_interaction_material']='STICK'
    config['destructible_factories']='false'
    config['disable_experience']='true'
    config['update_cycle']='20'
    config['repair_period']='2'
    config['culling_period']='1'
    config['factories'],config['recipes']=createFactorieAndRecipes()
    config['disabled_recipes']=[]
    config['enabled_recipes']=createCraftingRecipes()
    checkConflicts(config['factories'])
    print 'Fixing Conflicts...'
    fixConflicts(config['factories'])
    checkConflicts(config['factories'])
    ParseConfig.saveConfig(config)
    ParseConfig.prettyList(config)
    
def checkConflicts(factories):
    for factory in factories.values():
        for itemStack in factory.inputs:
            for otherFactory in factories.values():
                for otherItemStack in otherFactory.inputs:
                    if factory!=otherFactory:
                        if itemStack.equals(otherItemStack):
                            print 'Conflict  of '+factory.name+' and '+otherFactory.name
def fixConflicts(factories):
    for factory in factories.values():
        for itemStack in factory.inputs:
            for otherFactory in factories.values():
                for otherItemStack in otherFactory.inputs:
                    if factory!=otherFactory:
                        if itemStack.equals(otherItemStack):
                            itemStack.amount+=1

                            
if __name__ == '__main__':
    main()